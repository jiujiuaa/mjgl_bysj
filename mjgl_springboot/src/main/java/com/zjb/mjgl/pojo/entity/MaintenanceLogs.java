package com.zjb.mjgl.pojo.entity;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "模具保养操作记录表")
public class MaintenanceLogs {

    @ApiModelProperty(value = "保养记录ID")
    private String id;

    @ApiModelProperty(value = "模具ID，逻辑关联 molds.id")
    private String moldId;

    @ApiModelProperty(value = "关联计划ID（可选）")
    private String planId;

    @ApiModelProperty(value = "保养人ID，逻辑关联 users.id")
    private String maintainerId;

    @ApiModelProperty(value = "保养类型")
    private String maintenanceType;

    @ApiModelProperty(value = "详细内容")
    private String details;

    @ApiModelProperty(value = "实际开始时间")
    private Date actualStartTime;

    @ApiModelProperty(value = "实际结束时间")
    private Date actualEndTime;

    @ApiModelProperty(value = "费用（元）")
    private Double cost;

    @ApiModelProperty(value = "关联文件ID列表（JSON数组）")
    private String fileIds;

    @ApiModelProperty(value = "记录创建时间")
    private Date createdAt;

    @ApiModelProperty(value = "合理性审批状态：0=未审核, 1=合理, 2=存在问题")
    private Integer approvalStatus;

    @ApiModelProperty(value = "合理性审批意见")
    private String approvalComment;

    @ApiModelProperty(value = "合理性审批人ID")
    private String approverId;

    @ApiModelProperty(value = "合理性审批时间")
    private Date approvalTime;
}

