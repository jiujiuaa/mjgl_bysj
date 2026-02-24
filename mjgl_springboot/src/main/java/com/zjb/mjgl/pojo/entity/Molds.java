package com.zjb.mjgl.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "模具基本信息主表")
public class Molds {

    @ApiModelProperty(value = "模具唯一标识，32位UUID")
    private String id;

    @ApiModelProperty(value = "模具编号（业务唯一）")
    private String moldCode;

    @ApiModelProperty(value = "模具名称/型号")
    private String name;

    @ApiModelProperty(value = "类别：注塑模、冲压模等")
    private String category;

    @ApiModelProperty(value = "所属产品或项目")
    private String productProject;

    @ApiModelProperty(value = "存放位置（仓库-货架-库位）")
    private String location;

    @ApiModelProperty(value = "制造商")
    private String manufacturer;

    @ApiModelProperty(value = "供应商")
    private String supplier;

    @ApiModelProperty(value = "采购/入库日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime purchaseDate;

    @ApiModelProperty(value = "当前状态：1=在库, 2=使用中, 3=维修中, 4=外借, 5=待报废")
    private Integer currentStatus;

    @ApiModelProperty(value = "累计使用次数")
    private Integer totalUsageCount;

    @ApiModelProperty(value = "累计生产时长（小时）")
    private Double totalProductionTime;

    @ApiModelProperty(value = "最后保养日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMaintenanceDate;

    @ApiModelProperty(value = "采购成本（元）")
    private Double purchaseCost;

    @ApiModelProperty(value = "累计维修成本（元）")
    private Double totalRepairCost;

    @ApiModelProperty(value = "累计保养成本（元）")
    private Double totalMaintenanceCost;

    @ApiModelProperty(value = "创建人ID，逻辑关联 users.id")
    private String createdBy;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "最后更新时间")
    private LocalDateTime updatedAt;

}

