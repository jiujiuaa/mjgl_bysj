package com.zjb.mjgl.pojo.dto;

import com.zjb.mjgl.pojo.entity.Molds;
import com.zjb.mjgl.pojo.entity.MoldSpecs;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

/**
 * 模具创建和修改请求参数（对标 mall 的 *Param 风格）。
 * 继承 Molds 作为主表字段载体，并组合技术参数与二维码类型（一对一）。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MoldParam extends Molds {

    @ApiModelProperty("模具技术参数")
    private MoldSpecs specs;

    @ApiModelProperty("二维码类型：1=模具详情, 2=快速报修, 3=异常上报（一个模具对应一个二维码）")
    private Integer qrcodeType;

    @NotEmpty
    @ApiModelProperty(value = "模具编号", required = true)
    private String moldCode;
}

