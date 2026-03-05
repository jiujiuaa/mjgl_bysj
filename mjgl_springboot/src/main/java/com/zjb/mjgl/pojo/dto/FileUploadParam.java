package com.zjb.mjgl.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 文件上传参数（multipart/form-data，表单字段名与属性一致：files、bizType、bizId、fileType、description）。
 * 支持一次上传多个文件，共用同一组业务参数；支持多业务：模具、订单等通过 bizType + bizId 区分。
 */
@Data
@ApiModel("文件上传参数")
public class FileUploadParam {

    @NotEmpty(message = "请至少上传一个文件")
    @ApiModelProperty(value = "上传的文件（可多选）", required = true)
    private List<MultipartFile> files;

    @NotBlank(message = "业务类型不能为空")
    @ApiModelProperty(value = "业务类型：mold=模具, order=订单 等", required = true, example = "mold")
    private String bizType;

    @NotBlank(message = "业务ID不能为空")
    @ApiModelProperty(value = "业务主键ID（如模具id、订单id）", required = true)
    private String bizId;

    @NotBlank(message = "文件类型不能为空")
    @ApiModelProperty(value = "文件类型：photo/drawing/bom/manual/repair_photo", required = true, example = "photo")
    private String fileType;

    @ApiModelProperty(value = "文件说明", example = "模具正面照")
    private String description;

    @ApiModelProperty(value = "图片所属业务状态：如 REPORT/FINISH 等")
    private String imageStatus;
}
