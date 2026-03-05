package com.zjb.mjgl.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预警规则 VO
 */
@Data
public class AlertRuleVO {

    private String id;
    private String code;
    private String name;
    private String description;
    private String source;
    private Integer days;
    private Integer threshold;
    private Integer timeWindowMinutes;
    private BigDecimal valueThreshold;
    private String compareOp;
    private String metricType;
    private String metricField;
    private String triggerMode;
    private Integer enabled;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
