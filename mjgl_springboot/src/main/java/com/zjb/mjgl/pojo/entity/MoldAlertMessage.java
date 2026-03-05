package com.zjb.mjgl.pojo.entity;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoldAlertMessage {
    private String title;       // 标题，如 "温度异常"
    private String content;     // 内容，如 "模具M001温度达到185℃"
    private String type;        // 类型：ERROR, WARNING, INFO
    private Long id;            // 关联模具ID
    private String biz_type;
    private LocalDateTime time; // 发生时间
    /** 推送人ID（users.id） */
    private String senderId;
    /** 推送人用户名（username） */
    private String senderName;
}