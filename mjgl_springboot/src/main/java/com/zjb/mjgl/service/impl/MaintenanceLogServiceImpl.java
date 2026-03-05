package com.zjb.mjgl.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.common.enums.RoleEnum;
import com.zjb.mjgl.mapper.MaintenanceLogMapper;
import com.zjb.mjgl.mapper.MaintenanceReminderMapper;
import com.zjb.mjgl.pojo.dto.MaintenanceLogQueryParam;
import com.zjb.mjgl.pojo.entity.MaintenanceLogs;
import com.zjb.mjgl.pojo.entity.MoldAlertMessage;
import com.zjb.mjgl.pojo.entity.MaintenanceReminder;
import com.zjb.mjgl.pojo.vo.MaintenanceLogVO;
import com.zjb.mjgl.pojo.vo.MaintenanceReminderVO;
import com.zjb.mjgl.service.MaintenanceLogService;
import com.zjb.mjgl.service.NotificationService;
import com.zjb.mjgl.service.WebSocketMessageService;
import com.zjb.mjgl.utils.IdUtil;
import com.zjb.mjgl.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceLogServiceImpl implements MaintenanceLogService {
    private final MaintenanceLogMapper maintenanceLogMapper;
    private final MaintenanceReminderMapper maintenanceReminderMapper;
    private final WebSocketMessageService webSocketMessageService;
    private final NotificationService notificationService;
    @Override
    public Result<String> create(MaintenanceLogs maintenanceLogs) {
        if (maintenanceLogs == null) {
            return Result.fail("插入失败，请重试");
        }
        if (maintenanceLogs.getMoldId() == null || maintenanceLogs.getPlanId() == null) {
            return Result.fail("模具和保养计划不能为空");
        }
        log.info("开始创建保养记录, moldId={}, planId={}", maintenanceLogs.getMoldId(), maintenanceLogs.getPlanId());
        maintenanceLogs.setId(IdUtil.fastUUID());
        // 如果前端未传保养人，则默认当前登录用户
        if (maintenanceLogs.getMaintainerId() == null) {
            com.zjb.mjgl.pojo.entity.User currentUser = UserUtils.getCurrentUserDetails();
            if (currentUser != null) {
                maintenanceLogs.setMaintainerId(currentUser.getId());
            }
        }
        if (maintenanceLogs.getCreatedAt() == null) {
            maintenanceLogs.setCreatedAt(new java.util.Date());
        }
        try {
            int success = maintenanceLogMapper.insert(maintenanceLogs);
            if (success == 1) {
                // 保养记录创建成功后，尝试在同一条保养提醒记录上滚动下一周期
                rollMaintenanceReminderCycle(maintenanceLogs);

                MoldAlertMessage alert = new MoldAlertMessage();
                alert.setTitle("新建保养记录");
                alert.setContent("模具ID " + maintenanceLogs.getMoldId() + " 创建了一条新的保养记录");
                alert.setType("INFO");
                alert.setBiz_type("MAINTENANCE_LOG");
                alert.setTime(java.time.LocalDateTime.now());
                com.zjb.mjgl.pojo.entity.User currentUser = UserUtils.getCurrentUserDetails();
                if (currentUser != null) {
                    alert.setSenderId(currentUser.getId());
                    alert.setSenderName(currentUser.getUsername());
                }
                // 广播一份
                webSocketMessageService.sendAlert(alert);
                // 如果有保养人ID，再单播一份到该用户，并持久化一条通知
                if (maintenanceLogs.getMaintainerId() != null) {
                    String receiverId = maintenanceLogs.getMaintainerId();
                    webSocketMessageService.sendAlertToUser(receiverId, alert);
                    notificationService.createNotification(receiverId, alert);
                }
                log.info("创建保养记录成功, id={}", maintenanceLogs.getId());
                return Result.success(maintenanceLogs.getId());
            }
            log.warn("创建保养记录失败, 数据库插入失败, moldId={}, planId={}", maintenanceLogs.getMoldId(), maintenanceLogs.getPlanId());
            return Result.fail("插入失败，请检查网络后重试");
        } catch (Exception e) {
            log.error("创建保养记录失败: {}", maintenanceLogs, e);
            return Result.fail("插入失败，请重试");
        }
    }

    /**
     * 保养记录创建后：在同一条提醒记录上滚动下一周期，保持「一模具+一计划仅一条记录」。
     * - 按时间周期：next_due_date += interval_value 天
     * - 按使用次数：next_due_cycles += interval_value
     * - 状态重置为待处理，清空 sent_at、reminder_count
     */
    private void rollMaintenanceReminderCycle(MaintenanceLogs maintenanceLogs) {
        if (maintenanceLogs == null
                || maintenanceLogs.getMoldId() == null
                || maintenanceLogs.getPlanId() == null) {
            return;
        }
        String moldId = maintenanceLogs.getMoldId();
        String planId = maintenanceLogs.getPlanId();

        try {
            List<MaintenanceReminderVO> reminders =
                    Optional.ofNullable(maintenanceReminderMapper.listByMoldId(moldId))
                            .orElseGet(java.util.Collections::emptyList);

            MaintenanceReminderVO target = reminders.stream()
                    .filter(Objects::nonNull)
                    .filter(r -> planId.equals(r.getPlanId()))
                    .filter(r -> r.getStatus() == null || r.getStatus() == 1 || r.getStatus() == 2)
                    .min(Comparator.comparing(MaintenanceReminderVO::getCreatedAt,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .orElse(null);

            if (target == null) {
                log.debug("未找到可滚动的保养提醒记录, moldId={}, planId={}", moldId, planId);
                return;
            }

            Integer reminderType = target.getReminderType();
            Integer intervalValue = target.getIntervalValue();
            if (reminderType == null || intervalValue == null || intervalValue <= 0) {
                log.warn("保养提醒配置不完整, 跳过滚动, reminderId={}, type={}, interval={}",
                        target.getId(), reminderType, intervalValue);
                return;
            }

            Date now = new Date();
            ZoneId zoneId = ZoneId.systemDefault();
            MaintenanceReminder toUpdate = new MaintenanceReminder();
            toUpdate.setId(target.getId());

            if (Integer.valueOf(1).equals(reminderType)) {
                Date baseDate = Optional.ofNullable(target.getNextDueDate()).orElse(now);
                LocalDate baseLocalDate = baseDate.toInstant().atZone(zoneId).toLocalDate();
                LocalDate nextLocalDate = baseLocalDate.plusDays(intervalValue.longValue());
                toUpdate.setNextDueDate(Date.from(nextLocalDate.atStartOfDay(zoneId).toInstant()));
            } else if (Integer.valueOf(2).equals(reminderType)) {
                Integer baseCycles = Optional.ofNullable(target.getNextDueCycles()).orElse(0);
                toUpdate.setNextDueCycles(baseCycles + intervalValue);
            } else {
                log.warn("不支持的保养提醒类型, reminderId={}, type={}", target.getId(), reminderType);
                return;
            }

            toUpdate.setStatus(1); // 待处理
            toUpdate.setReminderCount(0);
            toUpdate.setSentAt(null);
            toUpdate.setUpdatedAt(now);
            maintenanceReminderMapper.update(toUpdate);
            log.info("保养记录创建后已在同一条提醒上滚动下一周期, reminderId={}", target.getId());
        } catch (Exception e) {
            log.warn("保养记录创建后滚动保养提醒失败, moldId={}, planId={}, err={}",
                    maintenanceLogs.getMoldId(), maintenanceLogs.getPlanId(), e.getMessage());
        }
    }

    @Override
    public Result<PageInfo<MaintenanceLogs>> getLogByMoldId(String id,Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        PageInfo<MaintenanceLogs> pageInfo = new PageInfo<>(maintenanceLogMapper.getByMoldId(id));
        return Result.success(pageInfo);
    }

    @Override
    public Result<PageInfo<MaintenanceLogs>> getLogByPlanId(String id,Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        PageInfo<MaintenanceLogs> pageInfo = new PageInfo<>(maintenanceLogMapper.getByPlanId(id));
        return Result.success(pageInfo);
    }

    @Override
    public PageInfo<MaintenanceLogVO> queryByCondition(MaintenanceLogQueryParam param, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(maintenanceLogMapper.queryByCondition(param));
    }

    @Override
    public Result<?> delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Result.fail("id为空");
        }
        log.info("删除保养记录, id={}", id);
        maintenanceLogMapper.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<?> update(MaintenanceLogs maintenanceLogs) {
        log.info("更新保养记录, id={}", maintenanceLogs.getId());
        maintenanceLogMapper.update(maintenanceLogs);
        return Result.success();
    }

    @Override
    public Result<?> approveMaintenanceLog(String id, Integer approvalStatus, String comment) {
        if (id == null || id.trim().isEmpty()) {
            return Result.fail("保养记录ID不能为空");
        }
        if (approvalStatus == null || approvalStatus < 0 || approvalStatus > 2) {
            return Result.fail("审批状态不合法");
        }
        log.info("开始审批保养记录, id={}, status={}, comment={}", id, approvalStatus, comment);
        com.zjb.mjgl.pojo.entity.User user = UserUtils.getCurrentUserDetails();
        if (user == null) {
            return Result.fail("未登录用户无法审批");
        }
        if (user.getRole() != RoleEnum.ADMIN) {
            return Result.fail("仅管理员可进行保养记录审批");
        }
        String trimmedComment = (comment != null && !comment.trim().isEmpty()) ? comment.trim() : null;
        int rows = maintenanceLogMapper.updateApproval(id, approvalStatus, trimmedComment, user.getId(), new java.util.Date());
        log.info("审批保养记录完成, id={}, status={}, rows={}", id, approvalStatus, rows);
        return rows > 0 ? Result.success() : Result.fail("更新合理性审批失败");
    }
}
