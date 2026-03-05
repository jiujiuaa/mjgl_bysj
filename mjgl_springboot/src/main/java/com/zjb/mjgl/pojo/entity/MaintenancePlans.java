package com.zjb.mjgl.pojo.entity;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "模具保养计划模板表")
public class MaintenancePlans {

    @ApiModelProperty(value = "保养计划ID")
    private String id;

    @ApiModelProperty(value = "计划名称，如“月度润滑保养”")
    private String name;

    @ApiModelProperty(value = "适用模具类型ID")
    private String moldTypeId;

    @ApiModelProperty(value = "指定模具ID（若填写则仅对该模具生效）")
    private String specificMoldId;

    @ApiModelProperty(value = "运行间隔小时数（如500）")
    private Integer intervalHours;

    @ApiModelProperty(value = "每月固定日（1-31）")
    private Integer scheduledDayOfMonth;

    @ApiModelProperty(value = "保养类型，如“润滑”、“清洁”")
    private String maintenanceType;

    @ApiModelProperty(value = "计划描述")
    private String description;

    @ApiModelProperty(value = "标准操作步骤（JSON格式）")
    private String standardProcedures;

    @ApiModelProperty(value = "预计耗时（小时）")
    private Double estimatedDurationHours;

    @ApiModelProperty(value = "所需物料清单（JSON格式）")
    private String requiredMaterials;

    @ApiModelProperty(value = "是否启用（0=停用，1=启用）")
    private Integer isActive;

    @ApiModelProperty(value = "创建人ID")
    private String createdBy;

    @ApiModelProperty(value = "创建时间")
    private Date createdAt;

    @ApiModelProperty(value = "更新时间")
    private Date updatedAt;


}

