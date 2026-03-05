package com.zjb.mjgl.service;

import com.zjb.mjgl.pojo.entity.MoldAlertMessage;
import com.zjb.mjgl.pojo.entity.UserNotification;

import java.util.List;

public interface NotificationService {

    /**
     * 为指定接收人创建一条通知（持久化），从告警消息中复制标题/内容/类型等信息。
     */
    void createNotification(String receiverUserId, MoldAlertMessage alert);

    /**
     * 查询某用户的未读通知列表（按时间倒序）
     */
    List<UserNotification> listUnread(String userId);

    /**
     * 查询某用户的全部通知列表（包含已读与未读，按时间倒序）
     */
    List<UserNotification> listAll(String userId);

    /**
     * 将通知标记为已读
     */
    void markRead(String id);
}

