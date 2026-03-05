
package com.zjb.mjgl.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("带模具信息的保养计划")
public class MaintenancePlanWithMoldVO {

    @ApiModelProperty("保养计划ID")
    private String id;

    @ApiModelProperty("计划名称")
    private String name;

    @ApiModelProperty("模具ID")
    private String moldId;

    @ApiModelProperty("模具名称")
    private String moldName;

    @ApiModelProperty("模具类别")
    private String moldCategory;

    @ApiModelProperty("运行间隔小时数")
    private Integer intervalHours;

    @ApiModelProperty("每月固定日")
    private Integer scheduledDayOfMonth;

    @ApiModelProperty("保养类型")
    private String maintenanceType;

    @ApiModelProperty("计划描述")
    private String description;

    @ApiModelProperty("标准操作步骤")
    private String standardProcedures;

    @ApiModelProperty("预计耗时（小时）")
    private Double estimatedDurationHours;

    @ApiModelProperty("所需物料清单")
    private String requiredMaterials;

    @ApiModelProperty("是否启用")
    private Integer isActive;

    @ApiModelProperty("创建人ID")
    private String createdBy;

    @ApiModelProperty("创建人姓名")
    private String createdByName;

    @ApiModelProperty("创建时间")
    private Date createdAt;

    @ApiModelProperty("更新时间")
    private Date updatedAt;
}

