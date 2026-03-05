package com.zjb.mjgl.pojo.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 按用户单播告警的请求体：userId + 告警内容
 */
@Data
public class SendAlertToUserDTO {
    /** 目标用户ID（必填） */
    private String userId;
    private String title;
    private String content;
    private String type;
    private Long id;
    private String biz_type;
    private LocalDateTime time;
    /** 可选：手动指定推送人ID/名称；不传则用当前登录用户填充 */
    private String senderId;
    private String senderName;
}
