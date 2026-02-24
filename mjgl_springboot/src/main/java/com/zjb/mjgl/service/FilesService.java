package com.zjb.mjgl.service;

import com.zjb.mjgl.pojo.dto.FileUploadParam;
import com.zjb.mjgl.pojo.vo.FileVO;

import javax.validation.Valid;
import java.util.List;

public interface FilesService {
    List<FileVO> upload(@Valid FileUploadParam param);

    void delete(List<String> fileIds);

    /**
     * 根据文件ID生成预览URL（MinIO 临时访问链接）
     */
    String generatePreviewUrl(String fileId);
}
