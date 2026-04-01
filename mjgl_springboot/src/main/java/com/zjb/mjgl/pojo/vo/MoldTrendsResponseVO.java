package com.zjb.mjgl.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class MoldTrendsResponseVO {

    private List<String> labels;

    private List<Integer> usageCounts;
    private List<Double> usageProductionHours;

    private List<Integer> repairCounts;
    private List<Double> avgRepairDurationHours;

    private List<Integer> maintenanceCounts;
}

