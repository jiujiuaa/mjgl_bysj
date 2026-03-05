package com.zjb.mjgl.pojo.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 新增/编辑预警规则参数。
 * repair/abnormal 必填 days、threshold；temperature/lubrication 必填 timeWindowMinutes、valueThreshold、compareOp、metricType；lubrication 还需 metricField。
 */
@Data
public class AlertRuleSaveParam {

    /** 编辑时必填 */
    private String id;

    @NotBlank(message = "规则编码不能为空")
    @Size(max = 64)
    private String code;

    @NotBlank(message = "规则名称不能为空")
    @Size(max = 128)
    private String name;

    @Size(max = 256)
    private String description;

    @NotBlank(message = "数据来源不能为空")
    @Size(max = 32)
    private String source;

    /** repair/abnormal 使用 */
    @Min(1)
    @Max(365)
    private Integer days;

    /** repair/abnormal 使用：次数阈值 */
    @Min(1)
    @Max(1000)
    private Integer threshold;

    /** temperature/lubrication 使用：统计天数(1～365) */
    @Min(1)
    @Max(365)
    private Integer timeWindowMinutes;

    /** temperature/lubrication 使用：数值阈值 */
    private BigDecimal valueThreshold;

    /** temperature/lubrication 使用：gt,ge,lt,le */
    @Size(max = 8)
    private String compareOp;

    /** temperature/lubrication 使用：max,min,avg */
    @Size(max = 16)
    private String metricType;

    /** lubrication 使用：oil_level_percent, pressure_kpa */
    @Size(max = 32)
    private String metricField;

    /** 温度/润滑使用：value-按聚合值, count-按次数(近N天有K次满足) */
    @Size(max = 16)
    private String triggerMode;

    @Min(0)
    @Max(1)
    private Integer enabled = 1;

    private Integer sortOrder = 0;
}
