package com.zjb.mjgl.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("异常记录视图")
public class MoldAbnormalRecordVO {

    @ApiModelProperty("记录ID")
    private String id;

    @ApiModelProperty("模具ID")
    private String moldId;

    @ApiModelProperty("模具编码")
    private String moldCode;

    @ApiModelProperty("模具名称")
    private String moldName;

    @ApiModelProperty("异常类型：1温度，2润滑，3其它")
    private Integer abnormalType;

    @ApiModelProperty("实际测量值，如 120℃ 或 30%, 3.5kPa")
    private String measuredValue;

    @ApiModelProperty("阈值或期望范围说明")
    private String thresholdValue;

    @ApiModelProperty("异常描述")
    private String description;

    @ApiModelProperty("异常发生时间")
    private Date occurredAt;

    @ApiModelProperty("来源：1自动监控，2温度巡检，3润滑巡检，4人工录入")
    private Integer sourceType;

    @ApiModelProperty("操作人ID")
    private String operatorId;

    @ApiModelProperty("操作人姓名")
    private String operatorName;

    @ApiModelProperty("创建时间")
    private Date createdAt;
}

