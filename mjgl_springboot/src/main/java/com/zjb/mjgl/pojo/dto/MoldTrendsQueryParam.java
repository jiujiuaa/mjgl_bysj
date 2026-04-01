package com.zjb.mjgl.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MoldTrendsQueryParam {

    @ApiModelProperty(value = "模具ID（可选：不填则统计全部模具聚合）")
    private String moldId;

    @ApiModelProperty(value = "统计开始日期（可选：不填则默认最近30天）")
    private LocalDate startDate;

    @ApiModelProperty(value = "统计结束日期（可选：不填则默认最近30天）")
    private LocalDate endDate;

    @ApiModelProperty(value = "时间桶类型：DAY / WEEK / MONTH（可选，默认 WEEK）")
    private String bucketType;
}

