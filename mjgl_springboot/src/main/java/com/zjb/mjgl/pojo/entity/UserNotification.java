package com.zjb.mjgl.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserNotification {
    private String id;
    private String userId;
    private String senderId;
    private String senderName;
    private String title;
    private String content;
    private String type;
    private String bizType;
    private Long moldId;
    /** 0 未读 1 已读 */
    private Integer readFlag;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}

