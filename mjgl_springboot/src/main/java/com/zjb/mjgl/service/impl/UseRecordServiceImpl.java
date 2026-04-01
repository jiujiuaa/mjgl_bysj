package com.zjb.mjgl.service.impl;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.common.enums.MoldStatusEnum;
import com.zjb.mjgl.common.enums.RoleEnum;
import com.zjb.mjgl.mapper.MoldsMapper;
import com.zjb.mjgl.mapper.UseRecordMapper;
import com.zjb.mjgl.pojo.dto.BatchIdsDTO;
import com.zjb.mjgl.pojo.dto.MoldUsageRecordDTO;
import com.zjb.mjgl.pojo.entity.MoldAlertMessage;
import com.zjb.mjgl.pojo.entity.MoldUsageRecords;
import com.zjb.mjgl.pojo.entity.Molds;
import com.zjb.mjgl.pojo.vo.MoldUsageRecordVO;
import com.zjb.mjgl.pojo.vo.UserVO;
import com.zjb.mjgl.service.AlertMessageService;
import com.zjb.mjgl.service.UseRecordService;
import com.zjb.mjgl.service.UserService;
import com.zjb.mjgl.utils.IdUtil;
import com.zjb.mjgl.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UseRecordServiceImpl implements UseRecordService {
    @Resource
    private UseRecordMapper useRecordMapper;
    @Resource
    private MoldsMapper moldsMapper;
    @Resource
    private AlertMessageService alertMessageService;
    @Resource
    private UserService userService;
    @Override
    public Result<Void> createRecord(MoldUsageRecordDTO moldUsageRecordDTO) {
        com.zjb.mjgl.pojo.entity.User currentUser = UserUtils.getCurrentUserDetails();
        if (currentUser == null) {
            return Result.fail("未登录用户无法创建使用记录");
        }
        RoleEnum role = currentUser.getRole();
        boolean canCreate = role == RoleEnum.ADMIN || role == RoleEnum.INSPECTOR || role == RoleEnum.PRODUCTION;
        if (!canCreate) {
            return Result.fail("当前用户无创建模具使用/借出记录权限");
        }
        if (moldUsageRecordDTO == null || moldUsageRecordDTO.getMoldId() == null) {
            return Result.fail("模具ID不能为空");
        }

        // 确保有主键 ID
        moldUsageRecordDTO.setId(
                Optional.ofNullable(moldUsageRecordDTO.getId())
                        .orElseGet(IdUtil::fastUUID)
        );

        // 填充当前登录用户作为申请人（ID + 名称）
        moldUsageRecordDTO.setApplicantId(currentUser.getId());
        // 这里优先使用真实姓名，其次使用用户名
        moldUsageRecordDTO.setApplicantName(
                Optional.ofNullable(currentUser.getRealName())
                        .filter(name -> !name.trim().isEmpty())
                        .orElse(currentUser.getUsername())
        );

        int status = moldsMapper.getStatus(moldUsageRecordDTO.getMoldId());
        if (status != MoldStatusEnum.IN_STOCK.getCode()) {
            return Result.fail("模具状态不允许借出");
        }
        int rows = useRecordMapper.insert(moldUsageRecordDTO);
        return rows > 0 ? Result.success() : Result.fail("创建模具使用/借出记录失败");
    }



    @Override
    public Result<String> updateStatus(String id, Integer status) {
        // 使用记录状态含义（mold_usage_records.status）：
        // 1 = 在库/待处理，2 = 使用中（含内部生产/外借进行中），3 = 使用完成（归还/结束）
        if (status == null || status < 1 || status > 3) {
            return Result.fail("不支持的使用记录状态");
        }

        com.zjb.mjgl.pojo.entity.User currentUser = UserUtils.getCurrentUserDetails();
        if (currentUser == null) {
            return Result.fail("未登录用户无法更新状态");
        }
        RoleEnum role = currentUser.getRole();
        boolean isManager = role == RoleEnum.ADMIN || role == RoleEnum.INSPECTOR;
        boolean canUpdate = isManager || role == RoleEnum.PRODUCTION;
        if (!canUpdate) {
            return Result.fail("当前用户无权限更新使用记录状态");
        }

        MoldUsageRecords record = useRecordMapper.getRecordById(id);
        if (record == null) {
            return Result.fail("使用记录不存在");
        }
        if (!isManager) {
            // 生产人员只能更新自己创建的记录
            String applicantId = record.getApplicantId();
            if (applicantId == null || !applicantId.equals(currentUser.getId())) {
                return Result.fail("无权限更新该记录");
            }
        }

        // 先更新使用记录状态，再根据“剩余在用记录”计算模具状态，避免归还时仍统计到旧状态。
        int recordRows = useRecordMapper.updateStatus(id, status);
        if (recordRows <= 0) {
            return Result.fail("更新使用记录状态失败");
        }

        // 根据 usage records 的“剩余 status=2”推导 molds.current_status
        int moldStatus = computeMoldStatusFromUsage(record.getMoldId());

        int moldRows = moldsMapper.updateStatus(record.getMoldId(), moldStatus);
        boolean success = moldRows > 0;
        if (success && status == 3) {
            notifyAdminsUsageNeedApproval(record.getMoldId(), id);
        }
        return new Result<>("OK", success, success ? "更新成功" : "更新失败");
    }

    /**
     * 使用记录进入“使用完成”时，向所有管理员发送“模具使用记录需要审批”通知
     */
    private void notifyAdminsUsageNeedApproval(String moldId, String recordId) {
        try {
            Molds mold = moldId != null && !moldId.trim().isEmpty()
                    ? moldsMapper.selectById(moldId)
                    : null;
            String code = mold != null ? mold.getMoldCode() : null;
            String name = mold != null ? mold.getName() : null;
            if (code != null) code = code.trim();
            if (name != null) name = name.trim();
            String moldInfo = (code != null && !code.isEmpty() ? code : "")
                    + (name != null && !name.isEmpty() ? (code != null && !code.isEmpty() ? " " : "") + name : "");
            if (moldInfo.isEmpty()) moldInfo = "未知模具";

            Result<List<UserVO>> usersResult = userService.getAllUsers();
            if (usersResult.getCode() != 200 || usersResult.getData() == null) return;
            List<String> adminIds = usersResult.getData().stream()
                    .filter(u -> u.getRole() == RoleEnum.ADMIN)
                    .map(UserVO::getId)
                    .filter(uid -> uid != null && !uid.trim().isEmpty())
                    .collect(Collectors.toList());
            if (adminIds.isEmpty()) return;

            MoldAlertMessage message = new MoldAlertMessage();
            message.setTitle("模具使用记录需要审批");
            message.setContent("模具「" + moldInfo + "」使用已结束，请进行合理性审批。");
            message.setType("INFO");
            message.setBiz_type("usage_approval");
            alertMessageService.sendAlertToUsers(adminIds, message);
        } catch (Exception e) {
            log.warn("通知管理员使用记录审批失败: {}", e.getMessage());
        }
    }

    @Override
    public Result<MoldUsageRecords> getRecordById(String id) {
        return Optional.ofNullable(useRecordMapper.getRecordById(id))
                .map(Result::success)
                .orElseGet(() -> Result.fail("未找到对应使用记录"));
    }

    @Override
    public Result<List<MoldUsageRecordVO>> listByMoldId(String moldId) {
        // 有管理权限的用户（ADMIN / INSPECTOR）可以查看所有记录，
        // 其他用户只能查看自己创建的记录
        List<MoldUsageRecordVO> records = Optional.ofNullable(UserUtils.getCurrentUserDetails())
                .map(user -> {
                    RoleEnum role = user.getRole();
                    boolean isManager = role == RoleEnum.ADMIN || role == RoleEnum.INSPECTOR;
                    return isManager
                            ? Optional.ofNullable(useRecordMapper.listByMoldId(moldId))
                                .orElseGet(Collections::emptyList)
                            : Optional.ofNullable(
                                        useRecordMapper.listByMoldIdAndApplicantId(moldId, user.getId()))
                                .orElseGet(Collections::emptyList);
                })
                .orElseGet(() -> Optional.ofNullable(useRecordMapper.listByMoldId(moldId))
                        .orElseGet(Collections::emptyList));

        // 再次用 Lambda 按创建时间倒序排一次，保证顺序
        List<MoldUsageRecordVO> sorted = records.stream()
                .sorted(Comparator.comparing(MoldUsageRecordVO::getCreatedAt,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .collect(Collectors.toList());

        return Result.success(sorted);
    }

    @Override
    public Result<List<MoldUsageRecordVO>> getAllRecord() {
        // 有管理权限的用户（ADMIN / INSPECTOR）可以查看所有记录，
        // 其他用户只能查看自己创建的记录
        List<MoldUsageRecordVO> records = Optional.ofNullable(UserUtils.getCurrentUserDetails())
                .map(user -> {
                    RoleEnum role = user.getRole();
                    boolean isManager = role == RoleEnum.ADMIN || role == RoleEnum.INSPECTOR;
                    return isManager
                            ? Optional.ofNullable(useRecordMapper.getAllRecord())
                                .orElseGet(Collections::emptyList)
                            : Optional.ofNullable(useRecordMapper.listByApplicantId(user.getId()))
                                .orElseGet(Collections::emptyList);
                })
                .orElseGet(() -> Optional.ofNullable(useRecordMapper.getAllRecord())
                        .orElseGet(Collections::emptyList));

        return Result.success(records);
    }

    @Override
    public Result<?> deleteRecord(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Result.fail("记录ID不能为空");
        }

        com.zjb.mjgl.pojo.entity.User currentUser = UserUtils.getCurrentUserDetails();
        if (currentUser == null) {
            return Result.fail("未登录用户无法删除记录");
        }
        RoleEnum role = currentUser.getRole();
        boolean isManager = role == RoleEnum.ADMIN || role == RoleEnum.INSPECTOR;
        boolean canDelete = isManager || role == RoleEnum.PRODUCTION;
        if (!canDelete) {
            return Result.fail("当前用户无权限删除使用记录");
        }

        MoldUsageRecords record = useRecordMapper.getRecordById(id);
        if (record == null) {
            return Result.fail("使用记录不存在");
        }
        if (!isManager) {
            String applicantId = record.getApplicantId();
            if (applicantId == null || !applicantId.equals(currentUser.getId())) {
                return Result.fail("无权限删除该记录");
            }
        }

        String moldId = record.getMoldId();
        int deletedRows = useRecordMapper.deleteById(id);
        if (deletedRows <= 0) {
            return Result.fail("删除使用记录失败");
        }

        // 同步更新模具状态：
        // - 如果该模具仍然存在 status=2 且 usage_type=2 的使用记录，则模具保持/设为“外借”(4)
        // - 否则若仍存在 status=2 且 usage_type!=2 的使用记录，则模具保持/设为“使用中”(2)
        // - 否则，将模具状态设为“在库”(1)
        Optional.ofNullable(moldId)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .ifPresent(mid -> {
                    int newMoldStatus = computeMoldStatusFromUsage(mid);
                    moldsMapper.updateStatus(mid, newMoldStatus);
                });

        return Result.success();
    }

    @Override
    public Result<?> deleteRecordsBatch(List<String> rawIds) {
        List<String> ids = BatchIdsDTO.normalizeList(rawIds);
        if (ids.isEmpty()) {
            return Result.fail("请选择要删除的使用记录");
        }
        return ids.stream()
                .map(this::deleteRecord)
                .filter(r -> r.getCode() != 200)
                .findFirst()
                .orElseGet(Result::success);
    }

    @Override
    public Result<?> updateUseRecord(MoldUsageRecordDTO moldUsageRecordDTO) {
        String moldId = moldUsageRecordDTO.getMoldId();
        String recordId = moldUsageRecordDTO.getId();
        if (recordId == null || recordId.trim().isEmpty()) {
            return Result.fail("使用记录ID不能为空");
        }
        MoldUsageRecords existingRecord = useRecordMapper.getRecordById(recordId);
        if (existingRecord == null) {
            return Result.fail("使用记录不存在");
        }

        com.zjb.mjgl.pojo.entity.User currentUser = UserUtils.getCurrentUserDetails();
        if (currentUser == null) {
            return Result.fail("未登录用户无法更新记录");
        }
        RoleEnum role = currentUser.getRole();
        boolean isManager = role == RoleEnum.ADMIN || role == RoleEnum.INSPECTOR;
        boolean canUpdate = isManager || role == RoleEnum.PRODUCTION;
        if (!canUpdate) {
            return Result.fail("当前用户无权限更新使用记录");
        }
        if (!isManager) {
            // 生产人员只能更新自己创建的记录
            String applicantId = existingRecord.getApplicantId();
            if (applicantId == null || !applicantId.equals(currentUser.getId())) {
                return Result.fail("无权限更新该记录");
            }
        }

        Molds mold = moldsMapper.selectById(moldId);
        if (mold == null) {
            return Result.fail("模具不存在");
        }
        if (moldUsageRecordDTO.getActualEndTime() != null && moldUsageRecordDTO.getActualStartTime() != null) {
            Long nowUseHours = diffHours(moldUsageRecordDTO.getActualStartTime(), moldUsageRecordDTO.getActualEndTime());
            if (nowUseHours != null) {
                long deltaHours = 0L;
                if (existingRecord.getActualStartTime() != null && existingRecord.getActualEndTime() != null) {
                    long lastUseHours = TimeUnit.MILLISECONDS.toHours(
                            existingRecord.getActualEndTime().getTime() - existingRecord.getActualStartTime().getTime());
                    deltaHours = nowUseHours - lastUseHours;
                } else {
                    deltaHours = nowUseHours;
                    mold.setTotalUsageCount(Optional.ofNullable(mold.getTotalUsageCount()).orElse(0) + 1);
                }
                mold.setTotalProductionTime(
                        Optional.ofNullable(mold.getTotalProductionTime()).orElse(0.0) + deltaHours);
                mold.setUpdatedAt(LocalDateTime.now());
                moldsMapper.updateMold(mold);
            }
        }
        int rows = useRecordMapper.update(moldUsageRecordDTO);
        return rows > 0 ? Result.success() : Result.fail("更新使用记录失败");
    }

    @Override
    public Result<?> approveUsage(String id, Integer approvalStatus, String comment) {
        if (id == null || id.trim().isEmpty()) {
            return Result.fail("使用记录ID不能为空");
        }
        if (approvalStatus == null || approvalStatus < 1 || approvalStatus > 2) {
            return Result.fail("审批状态不合法");
        }

        return Optional.ofNullable(UserUtils.getCurrentUserDetails())
                .map(user -> {
                    RoleEnum role = user.getRole();
                    boolean isManager = role == RoleEnum.ADMIN || role == RoleEnum.INSPECTOR;
                    if (!isManager) {
                        return Result.fail("当前用户无审批权限");
                    }
                    String trimmedComment = Optional.ofNullable(comment)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .orElse(null);

                    int rows = useRecordMapper.updateUsageApproval(
                            id,
                            approvalStatus,
                            trimmedComment,
                            user.getId(),
                            LocalDateTime.now()
                    );
                    return rows > 0 ? Result.success() : Result.fail("更新合理性审批失败");
                })
                .orElseGet(() -> Result.fail("未登录用户无法审批"));
    }

    public static Long diffHours(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return null;
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * 根据该模具现有 usage records 重新推导 molds.current_status（只考虑 usage 维度）
     * - status=2 且 usage_type=2 => 外借(4)
     * - status=2 且 usage_type!=2 => 使用中(2)
     * - 否则 => 在库(1)
     */
    private int computeMoldStatusFromUsage(String moldId) {
        if (moldId == null) return MoldStatusEnum.IN_STOCK.getCode();
        String trimmed = moldId.trim();
        if (trimmed.isEmpty()) return MoldStatusEnum.IN_STOCK.getCode();

        int lentOutCount = useRecordMapper.countInUseByMoldIdAndUsageType(trimmed, 2);
        if (lentOutCount > 0) {
            return MoldStatusEnum.LENT_OUT.getCode();
        }

        // 内部生产(1) / 试模(3) 都按“使用中(2)”处理
        int inUseCount =
                useRecordMapper.countInUseByMoldIdAndUsageType(trimmed, 1)
                        + useRecordMapper.countInUseByMoldIdAndUsageType(trimmed, 3);
        return inUseCount > 0
                ? MoldStatusEnum.IN_USE.getCode()
                : MoldStatusEnum.IN_STOCK.getCode();
    }
}
