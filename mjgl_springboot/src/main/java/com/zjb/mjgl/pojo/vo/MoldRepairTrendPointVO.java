package com.zjb.mjgl.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MoldRepairTrendPointVO {
    private String bucketKey;
    private Integer repairCount;
    private BigDecimal avgRepairDurationHours;
}

