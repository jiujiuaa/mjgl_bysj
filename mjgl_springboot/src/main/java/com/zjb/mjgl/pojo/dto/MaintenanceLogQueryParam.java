package com.zjb.mjgl.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode
public class MaintenanceLogQueryParam {

    /**
     * 计划名称 / 描述关键字模糊搜索（基于关联的保养计划）
     */
    private String keyword;

    /**
     * 指定模具（名称/编号模糊），基于保养计划关联的模具
     */
    private String specificMoldId;

    /**
     * 保养类型，如“润滑”、“清洁”等
     */
    private String maintenanceType;

    /**
     * 保养人ID
     */
    private String maintainerId;

    /**
     * 实际开始时间（起）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startActualTime;

    /**
     * 实际结束时间（止）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endActualTime;
}

