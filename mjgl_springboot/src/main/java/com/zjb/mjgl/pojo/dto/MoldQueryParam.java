package com.zjb.mjgl.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 模具查询参数，对标 mall 的 *QueryParam。
 */
@Data
@EqualsAndHashCode
public class MoldQueryParam {

    @ApiModelProperty("模具编号或名称模糊搜索")
    private String keyword;

    @ApiModelProperty("模具类别")
    private String category;

    @ApiModelProperty("当前状态：1=在库, 2=使用中, 3=维修中, 4=外借, 5=待报废")
    private Integer currentStatus;

    @ApiModelProperty("制造商")
    private String manuFacturer;

    @ApiModelProperty("所属产品或项目")
    private String productProject;

    @ApiModelProperty("创建人")
    private String createdBy;
}

