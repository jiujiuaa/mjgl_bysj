package com.zjb.mjgl.job;

import com.zjb.mjgl.common.enums.RoleEnum;
import com.zjb.mjgl.mapper.MaintenanceReminderMapper;
import com.zjb.mjgl.mapper.MoldsMapper;
import com.zjb.mjgl.pojo.entity.MaintenanceReminder;
import com.zjb.mjgl.pojo.entity.MoldAlertMessage;
import com.zjb.mjgl.pojo.entity.Molds;
import com.zjb.mjgl.pojo.vo.UserVO;
import com.zjb.mjgl.service.AlertMessageService;
import com.zjb.mjgl.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 每日扫描 maintenance_reminders，根据时间/使用次数阈值自动发送提醒。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceReminderJob {

    private final MaintenanceReminderMapper maintenanceReminderMapper;
    private final MoldsMapper moldsMapper;
    private final AlertMessageService alertMessageService;
    private final UserService userService;

    /**
     * 每天 03:00 扫描一次。
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void checkReminders() {
        log.info("开始执行保养提醒定时任务");
        List<MaintenanceReminder> reminders = Optional
                .ofNullable(maintenanceReminderMapper.listAll())
                .orElseGet(Collections::emptyList);
        if (reminders.isEmpty()) {
            log.info("保养提醒定时任务结束: 当前无任何保养提醒配置");
            return;
        }
        LocalDate today = LocalDate.now();
        long handledCount = reminders.stream()
                .filter(Objects::nonNull)
                // 跳过已完成或已忽略的
                .filter(r -> r.getStatus() == null || (r.getStatus() != 3 && r.getStatus() != 4))
                .peek(r -> log.debug("检查保养提醒记录, id={}, moldId={}, type={}", r.getId(), r.getMoldId(), r.getReminderType()))
                .map(r -> {
                    handleSingleReminder(r, today);
                    return r;
                })
                .count();
        log.info("保养提醒定时任务完成, 共检查 {} 条有效提醒记录", handledCount);
    }

    private void handleSingleReminder(MaintenanceReminder r, LocalDate today) {
        try {
            boolean needNotify = false;

            // 按时间周期：在 5 天内到期
            if (Integer.valueOf(1).equals(r.getReminderType()) && r.getNextDueDate() != null) {
                LocalDate dueDate = r.getNextDueDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                long days = ChronoUnit.DAYS.between(today, dueDate);
                if (days <= 5) {
                    needNotify = true;
                }
            }
            // 按使用次数：剩余使用次数 <= 10
            else if (Integer.valueOf(2).equals(r.getReminderType())
                    && r.getNextDueCycles() != null
                    && r.getMoldId() != null) {
                Molds mold = moldsMapper.selectById(r.getMoldId());
                int current = Optional.ofNullable(mold)
                        .map(Molds::getTotalUsageCount)
                        .orElse(0);
                int remaining = r.getNextDueCycles() - current;
                if (remaining <= 10) {
                    needNotify = true;
                }
            }

            if (!needNotify) {
                log.debug("保养提醒不满足触发条件, id={}, moldId={}", r.getId(), r.getMoldId());
                return;
            }

            sendAlert(r);

            // 记录一次提醒发送次数、时间，并置为已发送
            Integer count = Optional.ofNullable(r.getReminderCount()).orElse(0);
            r.setReminderCount(count + 1);
            r.setSentAt(new Date());
            r.setStatus(2); // 已发送
            r.setUpdatedAt(new Date());
            maintenanceReminderMapper.update(r);
            log.info("定时保养提醒已发送并更新记录, reminderId={}, 当前提醒次数={}", r.getId(), r.getReminderCount());
        } catch (Exception e) {
            log.warn("处理保养提醒失败, id={}: {}", r.getId(), e.getMessage());
        }
    }

    /**
     * 发送 WebSocket + 站内通知给所有管理员。
     */
    private void sendAlert(MaintenanceReminder r) {
        List<UserVO> users = Optional.ofNullable(userService.getAllUsers().getData())
                .orElseGet(Collections::emptyList);
        List<String> adminIds = users.stream()
                .filter(u -> u.getRole() == RoleEnum.ADMIN)
                .map(UserVO::getId)
                .filter(id -> id != null && !id.trim().isEmpty())
                .collect(Collectors.toList());
        if (adminIds.isEmpty()) {
            return;
        }

        MoldAlertMessage message = new MoldAlertMessage();
        message.setTitle("模具保养提醒");
        StringBuilder content = new StringBuilder();
        content.append("模具ID ").append(r.getMoldId())
                .append(" 的保养计划「").append(
                        Optional.ofNullable(r.getPlanName()).orElse("未命名计划")
                ).append("」即将到期。");
        if (Integer.valueOf(1).equals(r.getReminderType()) && r.getNextDueDate() != null) {
            content.append(" 下次保养日期：")
                    .append(r.getNextDueDate());
        } else if (Integer.valueOf(2).equals(r.getReminderType()) && r.getNextDueCycles() != null) {
            content.append(" 下次保养模次：")
                    .append(r.getNextDueCycles());
        }
        message.setContent(content.toString());
        message.setType("INFO");
        message.setBiz_type("MAINTENANCE_REMINDER");
        message.setTime(java.time.LocalDateTime.now());

        alertMessageService.sendAlertToUsers(adminIds, message);
    }
}

