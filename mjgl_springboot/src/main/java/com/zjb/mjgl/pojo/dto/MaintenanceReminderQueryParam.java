package com.zjb.mjgl.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 保养提醒查询条件
 */
@Data
@ApiModel("保养提醒查询参数")
public class MaintenanceReminderQueryParam {

    @ApiModelProperty("模具ID")
    private String moldId;

    @ApiModelProperty("模具名称/编号关键字")
    private String keyword;

    @ApiModelProperty("状态：1=待处理, 2=已发送, 3=已完成, 4=已忽略")
    private Integer status;

    @ApiModelProperty("提醒类型：1=按时间周期, 2=按使用次数")
    private Integer reminderType;
}
