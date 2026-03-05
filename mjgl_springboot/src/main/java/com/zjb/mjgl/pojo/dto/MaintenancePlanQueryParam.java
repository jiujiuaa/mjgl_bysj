package com.zjb.mjgl.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode
public class MaintenancePlanQueryParam {

    /**
     * 计划名称 / 描述关键字模糊搜索
     */
    private String keyword;

    /**
     * 适用模具类型ID，精确筛选该类型的保养计划
     */
    private String moldTypeId;

    /**
     * 指定模具ID（若填写则仅查询该模具的计划）
     */
    private String specificMoldId;

    /**
     * 保养类型，如“润滑”、“清洁”等
     */
    private String maintenanceType;

    /**
     * 是否启用（0=停用，1=启用），可为空表示全部
     */
    private Integer isActive;

    /**
     * 创建开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startCreatedAt;

    /**
     * 创建结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endCreatedAt;

    /**
     * 创建人ID，可选
     */
    private String createdBy;

    /**
     * 内部使用：按关联模具ID精确筛选计划（用于前端“新建保养记录”等场景）
     */
    private String bindMoldId;
}