package com.zjb.mjgl.service.impl;

import com.zjb.mjgl.mapper.UserNotificationMapper;
import com.zjb.mjgl.pojo.entity.MoldAlertMessage;
import com.zjb.mjgl.pojo.entity.UserNotification;
import com.zjb.mjgl.service.NotificationService;
import com.zjb.mjgl.utils.IdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final UserNotificationMapper userNotificationMapper;

    @Override
    public void createNotification(String receiverUserId, MoldAlertMessage alert) {
        if (receiverUserId == null || receiverUserId.trim().isEmpty() || alert == null) {
            return;
        }
        UserNotification n = new UserNotification();
        n.setId(IdUtil.fastUUID());
        n.setUserId(receiverUserId);
        n.setSenderId(alert.getSenderId());
        n.setSenderName(alert.getSenderName());
        n.setTitle(alert.getTitle());
        n.setContent(alert.getContent());
        n.setType(alert.getType());
        n.setBizType(alert.getBiz_type());
        n.setMoldId(alert.getId());
        n.setReadFlag(0);
        n.setCreatedAt(alert.getTime() != null ? alert.getTime() : LocalDateTime.now());
        userNotificationMapper.insert(n);
    }

    @Override
    public List<UserNotification> listUnread(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return userNotificationMapper.selectUnreadByUserId(userId);
    }

    @Override
    public List<UserNotification> listAll(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return userNotificationMapper.selectAllByUserId(userId);
    }

    @Override
    public void markRead(String id) {
        if (id == null || id.trim().isEmpty()) {
            return;
        }
        userNotificationMapper.markRead(id);
    }

    @Override
    public void markAllRead(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }
        userNotificationMapper.markAllReadByUserId(userId);
    }
}

