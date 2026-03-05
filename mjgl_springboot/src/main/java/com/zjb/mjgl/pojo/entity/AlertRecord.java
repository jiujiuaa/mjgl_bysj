package com.zjb.mjgl.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报警记录：与数据库 alerts 表一致
 * 模具异常报警与智能预测记录（状态 1=活跃, 2=已解决, 3=已忽略）
 */
@Data
public class AlertRecord {

    private String id;
    private String moldId;
    /** 报警类型: 1-故障频发, 2-保养超期, 3-温度异常 */
    private Integer alertType;
    /** 触发条件描述，如：近30天故障≥3次 */
    private String triggerCondition;
    /** 严重等级: 1=低, 2=中, 3=高 */
    private Integer severity;
    /** 提示消息 */
    private String message;
    /** 状态: 1=活跃, 2=已解决, 3=已忽略 */
    private Integer status;
    private String resolvedBy;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
