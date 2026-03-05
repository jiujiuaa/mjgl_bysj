package com.zjb.mjgl.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("保养记录列表视图（带名称）")
public class MaintenanceLogVO {

    @ApiModelProperty("保养记录ID")
    private String id;

    @ApiModelProperty("模具ID")
    private String moldId;

    @ApiModelProperty("模具编号")
    private String moldCode;

    @ApiModelProperty("模具名称")
    private String moldName;

    @ApiModelProperty("关联保养计划ID")
    private String planId;

    @ApiModelProperty("保养计划名称")
    private String planName;

    @ApiModelProperty("保养类型")
    private String maintenanceType;

    @ApiModelProperty("保养人ID")
    private String maintainerId;

    @ApiModelProperty("保养人姓名")
    private String maintainerName;

    @ApiModelProperty("保养详细内容")
    private String details;

    @ApiModelProperty("实际开始时间")
    private Date actualStartTime;

    @ApiModelProperty("实际结束时间")
    private Date actualEndTime;

    @ApiModelProperty("费用（元）")
    private Double cost;

    @ApiModelProperty("记录创建时间")
    private Date createdAt;

    @ApiModelProperty("合理性审批状态：0=未审核, 1=合理, 2=存在问题")
    private Integer approvalStatus;

    @ApiModelProperty("合理性审批意见")
    private String approvalComment;

    @ApiModelProperty("合理性审批人ID")
    private String approverId;

    @ApiModelProperty("合理性审批时间")
    private Date approvalTime;
}

