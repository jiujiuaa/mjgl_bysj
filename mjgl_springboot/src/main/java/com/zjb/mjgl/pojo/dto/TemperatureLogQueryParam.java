package com.zjb.mjgl.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("温度巡检记录查询参数")
public class TemperatureLogQueryParam {

    @ApiModelProperty("模具ID")
    private String moldId;

    @ApiModelProperty("模具名称/编号关键字")
    private String keyword;

    @ApiModelProperty("测点位置")
    private String sensorLocation;

    @ApiModelProperty("操作人ID")
    private String operatorId;

    @ApiModelProperty("开始时间(测量时间下限)")
    private Date startTime;

    @ApiModelProperty("结束时间(测量时间上限)")
    private Date endTime;
}

