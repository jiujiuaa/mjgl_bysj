package com.zjb.mjgl.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HealthFaultStatVO {
    private String moldId;
    private Integer faultCount;
    private BigDecimal repairCostTotal;
}

