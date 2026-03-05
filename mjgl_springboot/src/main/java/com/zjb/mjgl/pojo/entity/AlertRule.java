package com.zjb.mjgl.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能预警规则：支持手动定制统计天数/次数(repair,abnormal) 或 时间窗口+数值阈值(温度,润滑)
 */
@Data
public class AlertRule {

    private String id;
    private String code;
    private String name;
    private String description;
    /** 数据来源: repair-维修记录, abnormal-异常记录, temperature-温度, lubrication-润滑 */
    private String source;
    private Integer days;
    private Integer threshold;
    /** 时间窗口：temperature/lubrication 时为统计天数(1～365)，存于本字段 */
    private Integer timeWindowMinutes;
    /** 数值阈值，用于 temperature/lubrication */
    private java.math.BigDecimal valueThreshold;
    /** 比较符: gt,ge,lt,le */
    private String compareOp;
    /** 聚合方式: max,min,avg */
    private String metricType;
    /** 润滑指标: oil_level_percent, pressure_kpa */
    private String metricField;
    /** 温度/润滑触发方式: value-按聚合值, count-近N天内有K次满足 */
    private String triggerMode;
    /** 是否启用: 0-否, 1-是 */
    private Integer enabled;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
