package com.zjb.mjgl.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("异常记录查询参数")
public class MoldAbnormalRecordQueryParam {

    @ApiModelProperty("模具ID")
    private String moldId;

    @ApiModelProperty("模具名称/编号关键字")
    private String keyword;

    @ApiModelProperty("异常类型：1温度，2润滑，3其它")
    private Integer abnormalType;

    @ApiModelProperty("操作人ID")
    private String operatorId;

    @ApiModelProperty("来源：1自动监控，2温度巡检，3润滑巡检，4人工录入")
    private Integer sourceType;

    @ApiModelProperty("开始时间(异常发生时间下限)")
    private Date startTime;

    @ApiModelProperty("结束时间(异常发生时间上限)")
    private Date endTime;
}

