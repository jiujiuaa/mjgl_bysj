package com.zjb.mjgl.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 模具保养智能提醒记录表（一模具+一计划对应一条记录）。
 * 状态流转见 {@link com.zjb.mjgl.common.enums.MaintenanceReminderStatusEnum} 及 docs/maintenance_reminder_status_flow.md。
 */
@Data
@ApiModel(value = "模具保养智能提醒记录")
public class MaintenanceReminder {

    @ApiModelProperty(value = "保养提醒记录唯一ID，32位UUID")
    private String id;

    @ApiModelProperty(value = "关联模具ID，逻辑关联 molds.id")
    private String moldId;

    @ApiModelProperty(value = "关联保养计划ID，逻辑关联 maintenance_plans.id")
    private String planId;

    @ApiModelProperty(value = "保养计划名称，如：月度点检、年度大修")
    private String planName;

    @ApiModelProperty(value = "提醒类型：1=按时间周期, 2=按使用次数")
    private Integer reminderType;

    @ApiModelProperty(value = "间隔值，如30（天）或500（模次）")
    private Integer intervalValue;

    @ApiModelProperty(value = "下次应保养日期（当reminder_type=1时有效）")
    private Date nextDueDate;

    @ApiModelProperty(value = "下次应保养模次（当reminder_type=2时有效）")
    private Integer nextDueCycles;

    @ApiModelProperty(value = "自上次保养完成以来已发送的提醒次数")
    private Integer reminderCount;

    @ApiModelProperty(value = "状态：1=待处理, 2=已提醒, 3=已完成(保留), 4=已忽略，见 MaintenanceReminderStatusEnum")
    private Integer status;

    @ApiModelProperty(value = "实际推送提醒的时间")
    private Date sentAt;

    @ApiModelProperty(value = "处理人ID，逻辑关联 users.id")
    private String operatorId;

    @ApiModelProperty(value = "备注说明")
    private String notes;

    @ApiModelProperty(value = "记录创建时间")
    private Date createdAt;

    @ApiModelProperty(value = "最后更新时间")
    private Date updatedAt;
}
