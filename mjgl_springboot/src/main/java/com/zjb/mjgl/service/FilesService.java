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

    /**
     * 通用：根据业务类型 + 业务ID（可选文件类型）查询文件 VO 列表
     */
    List<FileVO> listByBiz(String bizType, String bizId, String fileType);
}
