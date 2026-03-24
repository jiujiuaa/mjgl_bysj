package com.zjb.mjgl.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HealthUsageStatVO {
    private String moldId;
    private Integer totalUsageCount;
    private BigDecimal totalProductionTime; // 小时
}

