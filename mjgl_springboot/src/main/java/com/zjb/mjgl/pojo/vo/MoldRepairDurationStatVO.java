package com.zjb.mjgl.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MoldRepairDurationStatVO {
    private String moldId;
    private Integer repairFrequency;
    private BigDecimal avgRepairDurationHours;
}

