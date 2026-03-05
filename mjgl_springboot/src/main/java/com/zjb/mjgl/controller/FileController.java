package com.zjb.mjgl.controller;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.FileUploadParam;
import com.zjb.mjgl.pojo.vo.FileVO;
import com.zjb.mjgl.service.FilesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 文件管理：上传、删除（通用，支持模具/订单等多业务通过 bizType+bizId 关联）
 */
@Api(tags = "文件管理")
@RestController
@RequestMapping("/api/files")
@Slf4j
public class FileController {


    @Autowired
    private FilesService filesService;
    /**
     * 上传文件并落库（MinIO + files 表），支持一次传多个文件
     * Content-Type: multipart/form-data，表单字段名与 FileUploadParam 一致：files（可多个）、bizType、bizId、fileType、description
     *
     * @param param 上传参数（含 files 与业务字段，多文件共用同一 bizType/bizId/fileType/description）
     * @return 本次上传生成的文件 VO 列表
     */
    @ApiOperation("上传文件（支持多选）")
    @PostMapping("/upload")
    public Result<List<FileVO>> upload(@Valid @ModelAttribute FileUploadParam param) {
        log.info("收到文件上传请求, bizType={}, bizId={}, fileType={}, 文件数量={}",
                param.getBizType(), param.getBizId(), param.getFileType(),
                param.getFiles() != null ? param.getFiles().size() : 0);
        return  Result.success(filesService.upload(param));
    }

    /**
     * 删除文件（支持传多个 id，传一个即删一个）
     * Content-Type: application/json，body 为文件 id 数组，示例：["id1", "id2"]
     *
     * @param fileIds 文件 id 列表
     */
    @ApiOperation("删除文件")
    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestBody List<String> fileIds) {
        log.info("收到删除文件请求, 数量={}", fileIds != null ? fileIds.size() : 0);
        filesService.delete(fileIds);
        return Result.success();
    }

    /**
     * 获取文件预览URL（前端可直接打开该URL进行预览）
     */
    @ApiOperation("获取文件预览URL")
    @GetMapping("/preview/{id}")
    public Result<String> preview(@PathVariable String id) {
        log.info("收到获取文件预览URL请求, id={}", id);
        String url = filesService.generatePreviewUrl(id);
        return Result.success(url);
    }

    /**
     * 通用：根据业务类型 + 业务ID（可选文件类型）查询文件列表
     */
    @ApiOperation("根据业务类型和业务ID查询文件列表")
    @GetMapping("/biz")
    public Result<List<FileVO>> listByBiz(@RequestParam String bizType,
                                          @RequestParam String bizId,
                                          @RequestParam(required = false) String fileType) {
        return Result.success(filesService.listByBiz(bizType, bizId, fileType));
    }
}
