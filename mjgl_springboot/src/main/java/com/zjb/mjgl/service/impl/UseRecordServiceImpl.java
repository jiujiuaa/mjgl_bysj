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
    public boolean createRecord(MoldUsageRecordDTO moldUsageRecordDTO) {
        // 确保有主键 ID
        moldUsageRecordDTO.setId(
                Optional.ofNullable(moldUsageRecordDTO.getId())
                        .orElseGet(IdUtil::fastUUID)
        );

        // 填充当前登录用户作为申请人（ID + 名称）
        Optional.ofNullable(UserUtils.getCurrentUserDetails()).ifPresent(user -> {
            moldUsageRecordDTO.setApplicantId(user.getId());
            // 这里优先使用真实姓名，其次使用用户名
            moldUsageRecordDTO.setApplicantName(
                    Optional.ofNullable(user.getRealName())
                            .filter(name -> !name.trim().isEmpty())
                            .orElse(user.getUsername())
            );
        });

        int status = moldsMapper.getStatus(moldUsageRecordDTO.getMoldId());
        if (status != MoldStatusEnum.IN_STOCK.getCode()) {
            return false;
        }
        return useRecordMapper.insert(moldUsageRecordDTO) > 0;
    }



    @Override
    public Result<String> updateStatus(String id, Integer status) {
        // 使用记录状态含义：
        // 1 = 模具在库，2 = 模具使用中，3 = 模具使用完成
        if (status == null || status < 1 || status > 3) {
            return Result.fail("不支持的使用记录状态");
        }

        return Optional.ofNullable(useRecordMapper.getRecordById(id))
                .map(record -> {
                    // 根据使用记录状态映射到模具当前状态
                    int moldStatus;
                    if (status == 2) {
                        // 使用中 -> 模具状态 = 使用中
                        moldStatus = MoldStatusEnum.IN_USE.getCode();
                    } else {
                        // 1 在库 / 3 使用完成 -> 模具状态都视为“在库”
                        moldStatus = MoldStatusEnum.IN_STOCK.getCode();
                    }

                    int moldRows = moldsMapper.updateStatus(record.getMoldId(), moldStatus);
                    int recordRows = useRecordMapper.updateStatus(id, status);
                    boolean success = moldRows > 0 && recordRows > 0;
                    if (success && status != null && status == 3) {
                        notifyAdminsUsageNeedApproval(record.getMoldId(), id);
                    }
                    return new Result<>("OK", success, success ? "更新成功" : "更新失败");
                })
                .orElseGet(() -> Result.fail("使用记录不存在"));
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

        return Optional.ofNullable(useRecordMapper.getRecordById(id))
                .map(record -> {
                    String moldId = record.getMoldId();

                    int deletedRows = useRecordMapper.deleteById(id);
                    if (deletedRows <= 0) {
                        return Result.fail("删除使用记录失败");
                    }

                    // 同步更新模具状态：
                    // - 如果该模具仍然存在状态为“使用中”(2) 的使用记录，则模具保持/设为“使用中”
                    // - 否则，将模具状态设为“在库”(1)
                    Optional.ofNullable(moldId)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .ifPresent(mid -> {
                                int inUseCount = useRecordMapper.countInUseByMoldId(mid);
                                int newMoldStatus = inUseCount > 0
                                        ? MoldStatusEnum.IN_USE.getCode()
                                        : MoldStatusEnum.IN_STOCK.getCode();
                                moldsMapper.updateStatus(mid, newMoldStatus);
                            });

                    return Result.success();
                })
                .orElseGet(() -> Result.fail("使用记录不存在"));
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
}
