package com.zjb.mjgl.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "模具技术参数表")
public class MoldSpecs {

    @ApiModelProperty(value = "参数记录ID")
    private String id;

    @ApiModelProperty(value = "模具ID，逻辑关联 molds.id")
    private String moldId;

    @ApiModelProperty(value = "外形尺寸（长×宽×高×重）")
    private String dimensions;

    @ApiModelProperty(value = "材质")
    private String material;

    @ApiModelProperty(value = "腔数")
    private Integer cavityCount;

    @ApiModelProperty(value = "设计寿命（次）")
    private Integer designLifeCycles;

    @ApiModelProperty(value = "设计寿命（年）")
    private Integer designLifeYears;

    @ApiModelProperty(value = "关键尺寸与公差")
    private String keyDimensions;

    @ApiModelProperty(value = "关联文档路径（JSON或逗号分隔）")
    private String linkedDocuments;


}

