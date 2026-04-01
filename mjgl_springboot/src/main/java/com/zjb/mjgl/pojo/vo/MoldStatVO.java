package com.zjb.mjgl.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MoldStatVO {

    private String moldId;
    private String moldCode;
    private String moldName;

    // 使用/生产
    private Integer totalUsageCount;
    private BigDecimal totalProductionTimeHours;

    // 维修
    private Integer repairFrequency;
    private BigDecimal avgRepairDurationHours;

    // 保养
    private Integer maintenancePlannedCount;
    private Integer maintenanceCompletedCount;
    private BigDecimal maintenanceRatePercent;
}

