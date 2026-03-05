package com.zjb.mjgl.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(value = "人工巡检温度记录表")
public class TemperatureLogs {

    @ApiModelProperty(value = "记录ID")
    private String id;

    @ApiModelProperty(value = "模具ID，逻辑关联 molds.id")
    private String moldId;

    @ApiModelProperty(value = "传感器/测点位置")
    private String sensorLocation;

    @ApiModelProperty(value = "温度(℃)")
    private Double temperature;

    @ApiModelProperty(value = "操作人ID，逻辑关联 users.id")
    private String operatorId;

    @ApiModelProperty(value = "实际测量时间")
    private Date operationTime;

    @ApiModelProperty(value = "记录创建时间")
    private Date createdAt;

    @ApiModelProperty(value = "备注说明")
    private String description;
}

