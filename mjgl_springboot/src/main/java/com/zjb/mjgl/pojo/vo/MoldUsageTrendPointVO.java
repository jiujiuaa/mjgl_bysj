package com.zjb.mjgl.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MoldUsageTrendPointVO {
    private String bucketKey;
    private Integer usageCount;
    private BigDecimal productionHours;
}

