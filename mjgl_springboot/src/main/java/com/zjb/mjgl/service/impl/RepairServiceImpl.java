package com.zjb.mjgl.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.common.enums.MoldStatusEnum;
import com.zjb.mjgl.common.enums.RepairStatusEnum;
import com.zjb.mjgl.common.enums.RoleEnum;
import com.zjb.mjgl.mapper.MoldsMapper;
import com.zjb.mjgl.mapper.RepairRecordMapper;
import com.zjb.mjgl.pojo.dto.BatchIdsDTO;
import com.zjb.mjgl.pojo.dto.RepairQueryParam;
import com.zjb.mjgl.pojo.dto.RepairRecordDTO;
import com.zjb.mjgl.pojo.entity.Molds;
import com.zjb.mjgl.pojo.entity.MoldAlertMessage;
import com.zjb.mjgl.pojo.vo.RepairRecordVO;
import com.zjb.mjgl.pojo.vo.UserVO;
import com.zjb.mjgl.service.AlertMessageService;
import com.zjb.mjgl.service.RepairService;
import com.zjb.mjgl.service.UserService;
import com.zjb.mjgl.utils.IdUtil;
import com.zjb.mjgl.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RepairServiceImpl implements RepairService {

    @Resource
    private RepairRecordMapper repairRecordMapper;

    @Resource
    private MoldsMapper moldsMapper;

    @Resource
    private AlertMessageService alertMessageService;

    @Resource
    private UserService userService;

    @Override
    public String createRepairRecord(RepairRecordDTO repairRecordDTO) {
        if (repairRecordDTO == null
                || repairRecordDTO.getMoldId() == null
                || repairRecordDTO.getMoldId().trim().isEmpty()) {
            log.warn("创建维修记录失败, 请求体为空或模具ID缺失");
            return null;
        }

        // 确保有主键ID
        repairRecordDTO.setId(IdUtil.fastUUID());

        // 送修人 = 当前登录用户（记录提交人）
        Optional.ofNullable(UserUtils.getCurrentUserDetails()).ifPresent(user ->
                repairRecordDTO.setReporterId(user.getId())
        );

        // 默认状态：待处理
        repairRecordDTO.setStatus(
                Optional.ofNullable(repairRecordDTO.getStatus())
                        .orElse(RepairStatusEnum.PENDING.getCode())
        );

        // 校验模具是否存在
        String moldId = repairRecordDTO.getMoldId();
        log.info("开始创建维修记录, moldId={}", moldId);
        Molds mold = moldsMapper.selectById(moldId);
        if (mold == null) {
            log.warn("创建维修记录失败, 模具不存在, moldId={}", moldId);
            return null;
        }

        int rows = repairRecordMapper.insert(repairRecordDTO);
        if (rows <= 0) {
            log.warn("创建维修记录失败, 数据库插入失败, moldId={}", moldId);
            return null;
        }

        // 新建维修记录后，将模具状态更新为“维修中”
        moldsMapper.updateStatus(moldId, MoldStatusEnum.UNDER_MAINTENANCE.getCode());
        log.info("创建维修记录成功, id={}, moldId={}", repairRecordDTO.getId(), moldId);
        return repairRecordDTO.getId();
    }

    @Override
    public Result<?> updeteRepairRecord(RepairRecordDTO repairRecordDTO) {
        if (repairRecordDTO == null
                || repairRecordDTO.getId() == null
                || repairRecordDTO.getId().trim().isEmpty()) {
            log.warn("更新维修记录失败, 请求体为空或ID缺失");
            return Result.fail("维修记录ID不能为空");
        }

        // 先查出旧记录，拿到当前状态和模具ID
        String id = repairRecordDTO.getId();
        log.info("开始更新维修记录, id={}", id);
        RepairRecordDTO existing = Optional.ofNullable(
                        repairRecordMapper.selectById(id))
                .orElse(null);
        if (existing == null) {
            log.warn("更新维修记录失败, 记录不存在, id={}", id);
            return Result.fail("维修记录不存在");
        }

        Integer oldStatus = Optional.ofNullable(existing.getStatus())
                .orElse(RepairStatusEnum.PENDING.getCode());
        Integer newStatus = Optional.ofNullable(repairRecordDTO.getStatus())
                .orElse(oldStatus);

        // 校验状态流转：允许 1->2, 2->3, 3->4，或从 3 回退到 2，或保持不变
        if (!newStatus.equals(oldStatus)) {
            boolean allowed =
                    (oldStatus.equals(RepairStatusEnum.PENDING.getCode())
                            && newStatus.equals(RepairStatusEnum.IN_PROGRESS.getCode()))
                            || (oldStatus.equals(RepairStatusEnum.IN_PROGRESS.getCode())
                            && newStatus.equals(RepairStatusEnum.FIXED.getCode()))
                            || (oldStatus.equals(RepairStatusEnum.FIXED.getCode())
                            && newStatus.equals(RepairStatusEnum.ACCEPTED.getCode()))
                            || (oldStatus.equals(RepairStatusEnum.FIXED.getCode())
                            && newStatus.equals(RepairStatusEnum.IN_PROGRESS.getCode()));
            if (!allowed) {
                log.warn("维修记录状态流转非法, id={}, oldStatus={}, newStatus={}", id, oldStatus, newStatus);
                return Result.fail("不允许从状态 " + oldStatus + " 直接流转到 " + newStatus);
            }
        }

        // 计算本次生效的 moldId（优先本次入参，其次旧记录）
        String moldId = Optional.ofNullable(repairRecordDTO.getMoldId())
                .filter(mId -> !mId.trim().isEmpty())
                .orElse(existing.getMoldId());
        if (moldId == null || moldId.trim().isEmpty()) {
            log.warn("更新维修记录失败, 模具ID缺失, id={}", id);
            return Result.fail("模具ID不能为空");
        }

        Molds mold = moldsMapper.selectById(moldId);
        if (mold == null) {
            log.warn("更新维修记录失败, 模具不存在, id={}, moldId={}", id, moldId);
            return Result.fail("模具不存在");
        }

        // 确保 DTO 中带上 moldId 和最终状态
        repairRecordDTO.setMoldId(moldId);
        repairRecordDTO.setStatus(newStatus);

        // 根据目标状态做字段补全/校验
        if (!newStatus.equals(oldStatus)) {
            if (newStatus.equals(RepairStatusEnum.IN_PROGRESS.getCode())) {
                // 进入“维修中”：需要有维修人，自动补开始时间
                String effectiveMaintainerId = Optional.ofNullable(repairRecordDTO.getMaintainerId())
                        .filter(s -> !s.trim().isEmpty())
                        .orElse(existing.getMaintainerId());
                if (effectiveMaintainerId == null || effectiveMaintainerId.trim().isEmpty()) {
                    return Result.fail("进入“维修中”状态时，维修人不能为空");
                }
                repairRecordDTO.setMaintainerId(effectiveMaintainerId);

                if (repairRecordDTO.getStartTime() == null && existing.getStartTime() == null) {
                    repairRecordDTO.setStartTime(LocalDateTime.now());
                }
            } else if (newStatus.equals(RepairStatusEnum.FIXED.getCode())) {
                // 进入“已修复”：如果没有结束时间，自动补当前时间
                if (repairRecordDTO.getEndTime() == null && existing.getEndTime() == null) {
                    repairRecordDTO.setEndTime(LocalDateTime.now());
                }
            } else if (newStatus.equals(RepairStatusEnum.ACCEPTED.getCode())) {
                // 进入“已验收”：需要有验证人
                String effectiveVerifierId = Optional.ofNullable(repairRecordDTO.getVerifierId())
                        .filter(s -> !s.trim().isEmpty())
                        .orElse(existing.getVerifierId());
                if (effectiveVerifierId == null || effectiveVerifierId.trim().isEmpty()) {
                    return Result.fail("进入“已验收”状态时，验证人不能为空");
                }
                repairRecordDTO.setVerifierId(effectiveVerifierId);
            }
        }

        // 如果状态发生变化，同步模具 current_status
        if (!newStatus.equals(oldStatus)) {
            int newMoldStatus = newStatus.equals(RepairStatusEnum.ACCEPTED.getCode())
                    ? MoldStatusEnum.IN_STOCK.getCode()
                    : MoldStatusEnum.UNDER_MAINTENANCE.getCode();
            moldsMapper.updateStatus(moldId, newMoldStatus);
        }

        int rows = repairRecordMapper.update(repairRecordDTO);
        if (rows <= 0) {
            log.warn("更新维修记录失败, 数据库更新失败, id={}", id);
            return Result.fail("更新维修记录失败");
        }
        // 维修记录进入“已验收”时，通知管理员进行合理性审批
        if (newStatus.equals(RepairStatusEnum.ACCEPTED.getCode())) {
            notifyAdminsRepairNeedApproval(moldId, repairRecordDTO.getId());
        }
        log.info("更新维修记录成功, id={}, newStatus={}", id, newStatus);
        return Result.success();
    }

    /**
     * 维修已验收时，向所有管理员发送“模具维修记录需要审批”通知（按 moldId 重新查询模具，确保编号、名称完整展示）
     */
    private void notifyAdminsRepairNeedApproval(String moldId, String repairRecordId) {
        try {
            Molds mold = moldId != null && !moldId.trim().isEmpty()
                    ? moldsMapper.selectById(moldId)
                    : null;
            String code = mold != null ? mold.getMoldCode() : null;
            String name = mold != null ? mold.getName() : null;
            if (code != null) {
                code = code.trim();
            }
            if (name != null) {
                name = name.trim();
            }
            String moldInfo = (code != null && !code.isEmpty() ? code : "")
                    + (name != null && !name.isEmpty() ? (code != null && !code.isEmpty() ? " " : "") + name : "");
            if (moldInfo.isEmpty()) {
                moldInfo = "未知模具";
            }

            Result<List<UserVO>> usersResult = userService.getAllUsers();
            if (usersResult.getCode() != 200 || usersResult.getData() == null) {
                log.warn("通知管理员维修审批失败, 获取用户列表失败, moldId={}, repairRecordId={}", moldId, repairRecordId);
                return;
            }
            List<String> adminIds = usersResult.getData().stream()
                    .filter(u -> u.getRole() == RoleEnum.ADMIN)
                    .map(UserVO::getId)
                    .filter(id -> id != null && !id.trim().isEmpty())
                    .collect(Collectors.toList());
            if (adminIds.isEmpty()) {
                log.warn("通知管理员维修审批失败, 未找到管理员用户, moldId={}, repairRecordId={}", moldId, repairRecordId);
                return;
            }
            MoldAlertMessage message = new MoldAlertMessage();
            message.setTitle("模具维修记录需要审批");
            message.setContent("模具「" + moldInfo + "」维修已验收，请进行合理性审批。");
            message.setType("INFO");
            message.setBiz_type("repair_approval");
            alertMessageService.sendAlertToUsers(adminIds, message);
        } catch (Exception e) {
            log.warn("通知管理员维修审批失败: {}", e.getMessage());
        }
    }

    @Override
    public Result<?> deteleMold(String id) {
        if(id==null || id.trim().isEmpty()){
            log.warn("删除维修记录失败, id 为空");
            return Result.fail("记录不存在");
        }
        log.info("删除维修记录, id={}", id);
        repairRecordMapper.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<?> deleteRecordsBatch(List<String> rawIds) {
        List<String> ids = BatchIdsDTO.normalizeList(rawIds);
        if (ids.isEmpty()) {
            return Result.fail("请选择要删除的维修记录");
        }
        ids.forEach(id -> deteleMold(id));
        return Result.success();
    }

    @Override
    public Result<List<RepairRecordVO>> getAllRecord() {
        List<RepairRecordVO> records = Optional.ofNullable(repairRecordMapper.getAll())
                .orElseGet(Collections::emptyList);

        // 使用枚举填充状态描述
        records.forEach(record -> Optional.ofNullable(record.getStatus())
                .map(RepairStatusEnum::fromCode)
                .ifPresent(enumVal -> record.setStatusDesc(enumVal.getDescription())));

        return Result.success(records);
    }

    @Override
    public PageInfo<RepairRecordVO> queryByCondition(RepairQueryParam repairQueryParam, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<RepairRecordVO> list = Optional.ofNullable(repairRecordMapper.selectDetailByCondition(repairQueryParam))
                .orElseGet(Collections::emptyList);
        list.forEach(record -> Optional.ofNullable(record.getStatus())
                .map(RepairStatusEnum::fromCode)
                .ifPresent(enumVal -> record.setStatusDesc(enumVal.getDescription())));
        return new PageInfo<>(list);
    }

    @Override
    public Result<List<RepairRecordVO>> getByMoldId(String moldId) {
        List<RepairRecordVO> list = Optional.ofNullable(repairRecordMapper.getrecordByMoldId(moldId))
                .orElseGet(Collections::emptyList);
        list.forEach(record -> Optional.ofNullable(record.getStatus())
                .map(RepairStatusEnum::fromCode)
                .ifPresent(enumVal -> record.setStatusDesc(enumVal.getDescription())));
        return Result.success(list);
    }

    @Override
    public Result<?> approveRepair(String id, Integer approvalStatus, String comment) {
        if (id == null || id.trim().isEmpty()) {
            return Result.fail("维修记录ID不能为空");
        }
        if (approvalStatus == null || approvalStatus < 0 || approvalStatus > 2) {
            return Result.fail("审批状态不合法");
        }
        log.info("开始审批维修记录, id={}, status={}, comment={}", id, approvalStatus, comment);
        return Optional.ofNullable(UserUtils.getCurrentUserDetails())
                .map(user -> {
                    if (user.getRole() != RoleEnum.ADMIN) {
                        return Result.fail("仅管理员可进行维修审批");
                    }
                    String trimmedComment = Optional.ofNullable(comment)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .orElse(null);
                    int rows = repairRecordMapper.updateRepairApproval(
                            id,
                            approvalStatus,
                            trimmedComment,
                            user.getId(),
                            LocalDateTime.now()
                    );
                    log.info("审批维修记录完成, id={}, status={}, rows={}", id, approvalStatus, rows);
                    return rows > 0 ? Result.success() : Result.fail("更新合理性审批失败");
                })
                .orElseGet(() -> Result.fail("未登录用户无法审批"));
    }
}

