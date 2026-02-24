package com.zjb.mjgl.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(value = "模具二维码定义表（支持多类型、可停用）")
public class MoldQrcodes {

    @ApiModelProperty(value = "二维码唯一ID（本身也是二维码内容）")
    private String id;

    @ApiModelProperty(value = "关联模具ID，逻辑关联 molds.id")
    private String moldId;

    @ApiModelProperty(value = "二维码类型：1=模具详情, 2=快速报修, 3=异常上报")
    private Integer qrcodeType;

    @ApiModelProperty(value = "是否有效：1=有效, 0=已停用（如模具报废或二维码损坏）")
    private Integer isActive;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdAt;

}

