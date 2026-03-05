package com.zjb.mjgl.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报警记录 VO（含模具名称、编号，与 alerts 表字段对应）
 */
@Data
public class AlertRecordVO {

    private String id;
    private String moldId;
    private String moldCode;
    private String moldName;
    private Integer alertType;
    private String triggerCondition;
    private Integer severity;
    private String message;
    private Integer status;
    private String resolvedBy;
    private String resolvedByName;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
