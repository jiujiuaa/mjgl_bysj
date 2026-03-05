package com.zjb.mjgl.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 保养提醒列表/详情 VO（带模具、处理人名称）
 */
@Data
@ApiModel("保养提醒列表视图")
public class MaintenanceReminderVO {

    @ApiModelProperty("提醒记录ID")
    private String id;

    @ApiModelProperty("模具ID")
    private String moldId;

    @ApiModelProperty("保养计划ID")
    private String planId;

    @ApiModelProperty("模具编号")
    private String moldCode;

    @ApiModelProperty("模具名称")
    private String moldName;

    @ApiModelProperty("保养计划名称")
    private String planName;

    @ApiModelProperty("提醒类型：1=按时间周期, 2=按使用次数")
    private Integer reminderType;

    @ApiModelProperty("间隔值")
    private Integer intervalValue;

    @ApiModelProperty("下次应保养日期")
    private Date nextDueDate;

    @ApiModelProperty("下次应保养模次")
    private Integer nextDueCycles;

    @ApiModelProperty("自上次保养完成以来已发送的提醒次数")
    private Integer reminderCount;

    @ApiModelProperty("状态：1=待处理, 2=已发送, 3=已完成, 4=已忽略")
    private Integer status;

    @ApiModelProperty("状态描述")
    private String statusDesc;

    @ApiModelProperty("实际推送时间")
    private Date sentAt;

    @ApiModelProperty("处理人ID")
    private String operatorId;

    @ApiModelProperty("处理人姓名")
    private String operatorName;

    @ApiModelProperty("备注")
    private String notes;

    @ApiModelProperty("创建时间")
    private Date createdAt;

    @ApiModelProperty("更新时间")
    private Date updatedAt;
}
