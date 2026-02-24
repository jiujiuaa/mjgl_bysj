package com.zjb.mjgl.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
@ApiModel(value = "通用业务文件表（模具/订单等）")
public class Files {

    @ApiModelProperty(value = "文件唯一ID")
    private String id;

    @ApiModelProperty(value = "业务类型：mold=模具, order=订单 等")
    private String bizType;

    @ApiModelProperty(value = "业务主键ID（如模具id、订单id）")
    private String bizId;

    @ApiModelProperty(value = "类型：photo/drawing/bom/manual/repair_photo")
    private String fileType;

    @ApiModelProperty(value = "原始文件名")
    private String originalName;

    @ApiModelProperty(value = "MinIO 对象名（用于删除）")
    private String fileName;

    @ApiModelProperty(value = "存储路径，如 /uploads/mold/xxx.jpg 或访问 URL")
    private String filePath;

    @ApiModelProperty(value = "文件大小（字节）")
    private Long fileSize;

    @ApiModelProperty(value = "上传人ID，逻辑关联 users.id")
    private String uploadUserId;

    @ApiModelProperty(value = "上传时间")
    private LocalDateTime uploadTime;

    @ApiModelProperty(value = "文件说明")
    private String description;

}

