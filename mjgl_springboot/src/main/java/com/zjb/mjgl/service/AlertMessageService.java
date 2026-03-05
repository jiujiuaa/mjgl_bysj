package com.zjb.mjgl.service;

import com.zjb.mjgl.pojo.dto.SendAlertToUserDTO;
import com.zjb.mjgl.pojo.entity.MoldAlertMessage;

import java.util.Collection;

/**
 * 统一封装的告警消息发送服务。
 * 其它业务 Service 可以直接注入本接口来发送 WebSocket 告警 + 持久化用户通知。
 */
public interface AlertMessageService {

    /**
     * 广播一条告警：推送到 /topic/alerts，并为所有用户写入一条通知。
     */
    void broadcastAlert(MoldAlertMessage message);

    /**
     * 按用户 ID 单播一条告警：WebSocket 单播 + 为该用户写入通知。
     */
    void sendAlertToUser(String userId, MoldAlertMessage message);

    /**
     * 按用户 ID 列表批量单播：每个用户一条 WebSocket 单播 + 一条通知。
     */
    void sendAlertToUsers(Collection<String> userIds, MoldAlertMessage message);

    /**
     * 从前端 DTO 解析并发送给多个用户（userId 支持逗号分隔）。
     * 方便 Controller 或其它地方直接使用 DTO。
     */
    void sendAlertByDto(SendAlertToUserDTO dto);
}

