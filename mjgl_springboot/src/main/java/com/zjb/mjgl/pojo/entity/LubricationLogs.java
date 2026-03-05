package com.zjb.mjgl.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(value = "人工巡检润滑记录表")
public class LubricationLogs {

    @ApiModelProperty(value = "记录ID")
    private String id;

    @ApiModelProperty(value = "模具ID，逻辑关联 molds.id")
    private String moldId;

    @ApiModelProperty(value = "润滑油液位百分比(0-100)")
    private Double oilLevelPercent;

    @ApiModelProperty(value = "压力(kPa)")
    private Double pressureKpa;

    @ApiModelProperty(value = "操作人ID，逻辑关联 users.id")
    private String operatorId;

    @ApiModelProperty(value = "实际巡检时间")
    private Date operationTime;

    @ApiModelProperty(value = "记录创建时间")
    private Date createdAt;

    @ApiModelProperty(value = "备注说明")
    private String description;
}

