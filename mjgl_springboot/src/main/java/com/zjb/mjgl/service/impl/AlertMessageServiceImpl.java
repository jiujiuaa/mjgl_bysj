package com.zjb.mjgl.service.impl;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.SendAlertToUserDTO;
import com.zjb.mjgl.pojo.entity.MoldAlertMessage;
import com.zjb.mjgl.pojo.vo.UserVO;
import com.zjb.mjgl.service.AlertMessageService;
import com.zjb.mjgl.service.NotificationService;
import com.zjb.mjgl.service.UserService;
import com.zjb.mjgl.service.WebSocketMessageService;
import com.zjb.mjgl.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 告警消息发送统一封装实现。
 * 负责：
 * - 填充时间、发送人等默认字段
 * - 调用 WebSocketMessageService 推送
 * - 调用 NotificationService 写入 user_notifications 表
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertMessageServiceImpl implements AlertMessageService {

    private final WebSocketMessageService webSocketMessageService;
    private final NotificationService notificationService;
    private final UserService userService;

    @Override
    public void broadcastAlert(MoldAlertMessage message) {
        MoldAlertMessage filled = fillDefaults(message);
        // WebSocket 广播
        webSocketMessageService.sendAlert(filled);

        // 为所有用户写一条通知
        Result<List<UserVO>> allUsersResult = userService.getAllUsers();
        if (allUsersResult.getCode() != 200 || allUsersResult.getData() == null) {
            log.warn("broadcastAlert: 获取用户列表失败，code={}, msg={}", allUsersResult.getCode(), allUsersResult.getMessage());
            return;
        }
        allUsersResult.getData().stream()
                .map(UserVO::getId)
                .filter(id -> id != null && !id.trim().isEmpty())
                .forEach(userId -> notificationService.createNotification(userId, filled));
    }

    @Override
    public void sendAlertToUser(String userId, MoldAlertMessage message) {
        if (userId == null || userId.trim().isEmpty()) {
            // 没有 userId 时退化为广播
            broadcastAlert(message);
            return;
        }
        MoldAlertMessage filled = fillDefaults(message);
        webSocketMessageService.sendAlertToUser(userId, filled);
        notificationService.createNotification(userId, filled);
    }

    @Override
    public void sendAlertToUsers(Collection<String> userIds, MoldAlertMessage message) {
        if (userIds == null || userIds.isEmpty()) {
            broadcastAlert(message);
            return;
        }
        MoldAlertMessage filled = fillDefaults(message);
        userIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(userId -> {
                    webSocketMessageService.sendAlertToUser(userId, filled);
                    notificationService.createNotification(userId, filled);
                });
    }

    @Override
    public void sendAlertByDto(SendAlertToUserDTO dto) {
        if (dto == null || dto.getUserId() == null || dto.getUserId().trim().isEmpty()) {
            log.warn("sendAlertByDto: userId 为空，忽略发送");
            return;
        }
        MoldAlertMessage message = new MoldAlertMessage();
        message.setTitle(dto.getTitle());
        message.setContent(dto.getContent());
        message.setType(dto.getType());
        message.setId(dto.getId());
        message.setBiz_type(dto.getBiz_type());
        message.setTime(dto.getTime());
        message.setSenderId(dto.getSenderId());
        message.setSenderName(dto.getSenderName());

        List<String> userIds = Stream.of(dto.getUserId().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        sendAlertToUsers(userIds, message);
    }

    /**
     * 填充告警的时间、发送人等默认字段，避免调用方每次都手动设置。
     */
    private MoldAlertMessage fillDefaults(MoldAlertMessage raw) {
        MoldAlertMessage message = raw != null ? raw : new MoldAlertMessage();
        if (message.getTime() == null) {
            message.setTime(LocalDateTime.now());
        }
        if (message.getSenderId() == null || message.getSenderId().trim().isEmpty()) {
            String currentId = UserUtils.getCurrentUserId();
            message.setSenderId(currentId);
        }
        if (message.getSenderName() == null || message.getSenderName().trim().isEmpty()) {
            String currentName = UserUtils.getCurrentUsername();
            message.setSenderName(currentName);
        }
        return message;
    }
}

