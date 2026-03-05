package com.zjb.mjgl.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.common.enums.RoleEnum;
import com.zjb.mjgl.mapper.MaintenanceReminderMapper;
import com.zjb.mjgl.mapper.MoldsMapper;
import com.zjb.mjgl.pojo.dto.MaintenanceReminderQueryParam;
import com.zjb.mjgl.pojo.entity.MaintenanceReminder;
import com.zjb.mjgl.pojo.entity.MoldAlertMessage;
import com.zjb.mjgl.pojo.entity.Molds;
import com.zjb.mjgl.pojo.vo.MaintenanceReminderVO;
import com.zjb.mjgl.pojo.vo.UserVO;
import com.zjb.mjgl.service.AlertMessageService;
import com.zjb.mjgl.service.MaintenanceReminderService;
import com.zjb.mjgl.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceReminderServiceImpl implements MaintenanceReminderService {

    private final MaintenanceReminderMapper maintenanceReminderMapper;
    private final MoldsMapper moldsMapper;
    private final AlertMessageService alertMessageService;
    private final UserService userService;

    @Override
    public PageInfo<MaintenanceReminderVO> queryByCondition(MaintenanceReminderQueryParam param, int pageNum, int pageSize) {
        log.info("查询保养提醒列表, pageNum={}, pageSize={}, 条件={}", pageNum, pageSize, param);
        PageHelper.startPage(pageNum, pageSize);
        MaintenanceReminderQueryParam effective = Optional.ofNullable(param)
                .orElseGet(MaintenanceReminderQueryParam::new);
        List<MaintenanceReminderVO> list = Optional.ofNullable(maintenanceReminderMapper.queryByCondition(effective))
                .orElseGet(Collections::emptyList);
        log.info("查询保养提醒完成, 共 {} 条记录", list.size());
        return new PageInfo<>(list);
    }

    @Override
    public Result<List<MaintenanceReminderVO>> listByMoldId(String moldId) {
        if (moldId == null || moldId.trim().isEmpty()) {
            log.warn("按模具ID查询保养提醒失败, moldId 为空");
            return Result.fail("模具ID不能为空");
        }
        String trimmedId = moldId.trim();
        log.info("按模具ID查询保养提醒, moldId={}", trimmedId);
        List<MaintenanceReminderVO> list = Optional.ofNullable(maintenanceReminderMapper.listByMoldId(trimmedId))
                .orElseGet(Collections::emptyList);
        log.info("按模具ID查询保养提醒完成, moldId={}, 共 {} 条记录", trimmedId, list.size());
        return Result.success(list);
    }

    @Override
    public Result<MaintenanceReminder> getById(String id) {
        if (id == null || id.trim().isEmpty()) {
            log.warn("根据ID查询保养提醒失败, id 为空");
            return Result.fail("提醒记录ID不能为空");
        }
        String trimmedId = id.trim();
        log.info("根据ID查询保养提醒, id={}", trimmedId);
        MaintenanceReminder entity = maintenanceReminderMapper.selectById(trimmedId);
        if (entity != null) {
            log.info("根据ID查询保养提醒成功, id={}", trimmedId);
            return Result.success(entity);
        } else {
            log.warn("根据ID查询保养提醒失败, 未找到记录, id={}", trimmedId);
            return Result.fail("未找到该提醒记录");
        }
    }

    @Override
    public Result<?> sendReminderNow(String id) {
        if (id == null || id.trim().isEmpty()) {
            log.warn("手动发送保养提醒失败, id 为空");
            return Result.fail("提醒记录ID不能为空");
        }
        String trimmedId = id.trim();
        log.info("准备手动发送保养提醒, id={}", trimmedId);
        MaintenanceReminder r = maintenanceReminderMapper.selectById(trimmedId);
        if (r == null) {
            log.warn("手动发送保养提醒失败, 未找到记录, id={}", trimmedId);
            return Result.fail("未找到该提醒记录");
        }
        sendAlert(r, true);
        // 主动发送时，增加一次提醒计数，记录最后推送时间，并置为已发送
        Integer count = Optional.ofNullable(r.getReminderCount()).orElse(0);
        r.setReminderCount(count + 1);
        r.setSentAt(new Date());
        r.setStatus(2); // 已发送
        r.setUpdatedAt(new Date());
        maintenanceReminderMapper.update(r);
        log.info("手动发送保养提醒完成, id={}, 当前提醒次数={}", trimmedId, r.getReminderCount());
        return Result.success();
    }

    @Override
    public Result<?> ignoreReminder(String id) {
        if (id == null || id.trim().isEmpty()) {
            log.warn("忽略保养提醒失败, id 为空");
            return Result.fail("提醒记录ID不能为空");
        }
        String trimmedId = id.trim();
        MaintenanceReminder r = maintenanceReminderMapper.selectById(trimmedId);
        if (r == null) {
            log.warn("忽略保养提醒失败, 未找到记录, id={}", trimmedId);
            return Result.fail("未找到该提醒记录");
        }
        Integer status = r.getStatus();
        if (Integer.valueOf(3).equals(status) || Integer.valueOf(4).equals(status)) {
            log.warn("忽略保养提醒失败, 当前状态不可忽略, id={}, status={}", trimmedId, status);
            return Result.fail("已完成或已忽略的提醒不能再次忽略");
        }
        r.setStatus(4); // 已忽略
        r.setUpdatedAt(new Date());
        maintenanceReminderMapper.update(r);
        log.info("保养提醒已忽略, id={}", trimmedId);
        return Result.success();
    }

    /**
     * 发送 WebSocket + 站内通知给所有管理员。
     * manual 标识是否为手动触发，仅用于文案区分。
     */
    private void sendAlert(MaintenanceReminder r, boolean manual) {
        log.info("开始发送保养提醒消息, reminderId={}, manual={}", r.getId(), manual);
        List<UserVO> users = Optional.ofNullable(userService.getAllUsers().getData())
                .orElseGet(Collections::emptyList);
        List<String> adminIds = users.stream()
                .filter(u -> u.getRole() == RoleEnum.ADMIN)
                .map(UserVO::getId)
                .filter(id -> id != null && !id.trim().isEmpty())
                .collect(Collectors.toList());
        if (adminIds.isEmpty()) {
            log.warn("发送保养提醒消息失败, 未找到管理员用户, reminderId={}", r.getId());
            return;
        }

        MoldAlertMessage message = new MoldAlertMessage();
        message.setTitle(manual ? "手动触发的保养提醒" : "模具保养提醒");
        StringBuilder content = new StringBuilder();
        content.append("模具ID ").append(r.getMoldId())
                .append(" 的保养计划「").append(
                        Optional.ofNullable(r.getPlanName()).orElse("未命名计划")
                ).append("」");
        if (manual) {
            content.append("有一条手动触发的保养提醒，请尽快确认。");
        } else {
            content.append("即将到期，请尽快安排保养。");
        }

        if (Integer.valueOf(1).equals(r.getReminderType()) && r.getNextDueDate() != null) {
            content.append(" 下次保养日期：").append(r.getNextDueDate());
        } else if (Integer.valueOf(2).equals(r.getReminderType()) && r.getNextDueCycles() != null) {
            Molds mold = moldsMapper.selectById(r.getMoldId());
            int current = Optional.ofNullable(mold)
                    .map(Molds::getTotalUsageCount)
                    .orElse(0);
            int remaining = r.getNextDueCycles() - current;
            content.append(" 下次保养模次：").append(r.getNextDueCycles())
                    .append("，当前累计使用：").append(current)
                    .append("，距离保养还剩约 ").append(remaining).append(" 次。");
        }

        message.setContent(content.toString());
        message.setType("INFO");
        message.setBiz_type("MAINTENANCE_REMINDER");
        message.setTime(java.time.LocalDateTime.now());

        alertMessageService.sendAlertToUsers(adminIds, message);
        log.info("保养提醒消息发送完成, reminderId={}, 发送管理员数量={}", r.getId(), adminIds.size());
    }
}

