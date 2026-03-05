package com.zjb.mjgl.service;

import com.zjb.mjgl.pojo.entity.MoldAlertMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket 消息发送服务
 * 通过 SimpMessagingTemplate 向指定 destination 推送消息，供前端订阅接收。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketMessageService {

    private final SimpMessagingTemplate messagingTemplate;

    /** 广播主题：所有订阅了 /topic/alerts 的客户端都会收到 */
    public static final String TOPIC_ALERTS = "/topic/alerts";
    /** 按用户ID单播的前缀：/topic/alerts.user.{userId} */
    public static final String USER_ALERT_PREFIX = "/topic/alerts.user.";

    /**
     * 向主题广播消息（所有订阅该主题的客户端都会收到）
     */
    public void sendToTopic(String destination, Object payload) {
        messagingTemplate.convertAndSend(destination, payload);
        log.debug("WebSocket 已发送到 {}: {}", destination, payload);
    }

    /**
     * 向「模具告警」主题推送一条告警消息（使用项目已有的 MoldAlertMessage）
     */
    public void sendAlert(MoldAlertMessage message) {
        sendToTopic(TOPIC_ALERTS, message);
        log.info("已推送告警: {} - {}", message.getTitle(), message.getContent());
    }

    /**
     * 向指定用户ID的专属通道发送告警消息（单播）
     */
    public void sendAlertToUser(String userId, MoldAlertMessage message) {
        if (userId == null || userId.trim().isEmpty()) {
            sendAlert(message);
            return;
        }
        String destination = USER_ALERT_PREFIX + userId;
        sendToTopic(destination, message);
        log.info("已向用户 {} 推送告警: {} - {}", userId, message.getTitle(), message.getContent());
    }
}
