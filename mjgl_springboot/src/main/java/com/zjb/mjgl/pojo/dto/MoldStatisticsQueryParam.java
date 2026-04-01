package com.zjb.mjgl.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MoldStatisticsQueryParam {

    @ApiModelProperty(value = "模具ID（可选：不填则统计全部模具）")
    private String moldId;

    @ApiModelProperty(value = "统计开始日期（可选：不填则默认最近30天）")
    private LocalDate startDate;

    @ApiModelProperty(value = "统计结束日期（可选：不填则默认最近30天）")
    private LocalDate endDate;
}

