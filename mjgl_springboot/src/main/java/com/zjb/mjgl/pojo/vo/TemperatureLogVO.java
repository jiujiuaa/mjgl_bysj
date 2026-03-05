package com.zjb.mjgl.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("温度巡检记录视图")
public class TemperatureLogVO {

    @ApiModelProperty("记录ID")
    private String id;

    @ApiModelProperty("模具ID")
    private String moldId;

    @ApiModelProperty("模具编码")
    private String moldCode;

    @ApiModelProperty("模具名称")
    private String moldName;

    @ApiModelProperty("传感器/测点位置")
    private String sensorLocation;

    @ApiModelProperty("温度(℃)")
    private Double temperature;

    @ApiModelProperty("操作人ID")
    private String operatorId;

    @ApiModelProperty("操作人姓名")
    private String operatorName;

    @ApiModelProperty("测量时间")
    private Date operationTime;

    @ApiModelProperty("记录创建时间")
    private Date createdAt;

    @ApiModelProperty("备注说明")
    private String description;
}

