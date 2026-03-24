package com.zjb.mjgl.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HealthReportGenerateParam {

    @ApiModelProperty(value = "周期类型：WEEKLY/MONTHLY/QUARTERLY")
    private String periodType;

    @ApiModelProperty(value = "自定义周期开始日期（可选）")
    private LocalDate periodStart;

    @ApiModelProperty(value = "自定义周期结束日期（可选）")
    private LocalDate periodEnd;

    @ApiModelProperty(value = "模具ID（可选：不填则全量生成）")
    private String moldId;

    @ApiModelProperty(value = "是否同步导出 PDF 并归档（默认 true）")
    private Boolean exportPdf;
}

