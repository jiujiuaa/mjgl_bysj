package com.zjb.mjgl.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HealthReportQueryParam {

    @ApiModelProperty(value = "模具ID（可选）")
    private String moldId;

    @ApiModelProperty(value = "报告状态：1=草稿,2=已生成,3=已导出（可选）")
    private Integer status;

    @ApiModelProperty(value = "统计周期开始日期（可选）")
    private LocalDate periodStart;

    @ApiModelProperty(value = "统计周期结束日期（可选）")
    private LocalDate periodEnd;

    @ApiModelProperty(value = "健康分最小值（可选）")
    private Integer minHealthScore;

    @ApiModelProperty(value = "健康分最大值（可选）")
    private Integer maxHealthScore;
}

