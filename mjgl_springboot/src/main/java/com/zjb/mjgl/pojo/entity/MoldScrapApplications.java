package com.zjb.mjgl.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(value = "模具报废申请表")
public class MoldScrapApplications {

    @ApiModelProperty(value = "报废申请ID，UUID")
    private String id;

    @ApiModelProperty(value = "模具ID")
    private String moldId;

    @ApiModelProperty(value = "申请人ID")
    private String applicantId;

    @ApiModelProperty(value = "申请人姓名（冗余）")
    private String applicantName;

    @ApiModelProperty(value = "申请原因")
    private String reason;

    @ApiModelProperty(value = "报废申请状态：1=待审批,2=已批准,3=已拒绝,4=已执行")
    private Integer status;

    @ApiModelProperty(value = "审批人ID")
    private String approverId;

    @ApiModelProperty(value = "审批人姓名（冗余）")
    private String approverName;

    @ApiModelProperty(value = "审批意见")
    private String approvalComment;

    @ApiModelProperty(value = "审批时间")
    private LocalDateTime approvedAt;

    @ApiModelProperty(value = "处理/执行人ID")
    private String handlerId;

    @ApiModelProperty(value = "处理/执行人姓名（冗余）")
    private String handlerName;

    @ApiModelProperty(value = "执行备注/处理意见")
    private String handlerComment;

    @ApiModelProperty(value = "执行时间")
    private LocalDateTime handledAt;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "最后更新时间")
    private LocalDateTime updatedAt;
}

