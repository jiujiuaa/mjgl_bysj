package com.zjb.mjgl.pojo.entity;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "模具使用/借出记录表")
public class MoldUsageRecords {

    @ApiModelProperty(value = "使用记录ID，UUID")
    private String id;

    @ApiModelProperty(value = "关联模具ID")
    private String moldId;

    private Integer usageType;

    @ApiModelProperty(value = "预计开始时间")
    private Date scheduledStartTime;

    @ApiModelProperty(value = "预计结束时间")
    private Date scheduledEndTime;

    @ApiModelProperty(value = "实际开始时间（借出/上机）")
    private Date actualStartTime;

    @ApiModelProperty(value = "实际结束时间（归还）")
    private Date actualEndTime;

    @ApiModelProperty(value = "申请人ID（当前用户）")
    private String applicantId;

    @ApiModelProperty(value = "借用人姓名（外借时填写）")
    private String borrowerName;

    @ApiModelProperty(value = "借用单位")
    private String borrowerCompany;

    @ApiModelProperty(value = "用途说明")
    private String purpose;

    private Integer status;

    @ApiModelProperty(value = "1=通过, 0=未通过")
    private Integer returnInspectionPassed;

    @ApiModelProperty(value = "归还备注")
    private String returnRemarks;

    private Date createdAt;

    private Date updatedAt;


}

