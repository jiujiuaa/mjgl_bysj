package com.zjb.mjgl.service.impl;

import com.zjb.mjgl.mapper.FilesMapper;
import com.zjb.mjgl.pojo.dto.FileUploadParam;
import com.zjb.mjgl.pojo.entity.Files;
import com.zjb.mjgl.pojo.vo.FileVO;
import com.zjb.mjgl.service.FilesService;
import com.zjb.mjgl.utils.IdUtil;
import com.zjb.mjgl.utils.MinioUtil;
import com.zjb.mjgl.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilesServiceImpl implements FilesService {

    @Resource
    private MinioUtil minioUtil;
    @Resource
    private FilesMapper filesMapper;
    @Override
    public List<FileVO> upload(FileUploadParam param) {
        //上传文件到minio
        List<Map<String, String>> maps = minioUtil.uploadFiles(param.getFiles());
        List<Files> files = maps.stream().map(map -> Files.builder()
                .id(IdUtil.fastUUID())
                .bizType(param.getBizType())
                .bizId(param.getBizId())
                .fileType(param.getFileType())
                .originalName(map.get("originalFilename"))
                .fileName(map.get("fileName"))
                .filePath(map.get("filePath"))
                .fileSize(Long.parseLong(map.get("fileSize")))
                .uploadUserId(UserUtils.getCurrentUserId())
                .uploadTime(LocalDateTime.now())
                .description(param.getDescription())
                .imageStatus(param.getImageStatus())
                .build()
        ).collect(Collectors.toList());

        filesMapper.insertBatch(files);
        // 使用 BeanUtils 将 Files 列表拷贝为 FileVO 列表返回前端
        return files.stream()
                .map(f -> {
                    FileVO vo = new FileVO();
                    BeanUtils.copyProperties(f, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void delete(List<String> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }
        // 1. 查询待删文件，拿到 MinIO 对象名
        List<Files> files = filesMapper.selectByIds(fileIds);
        List<String> objectNames = files.stream()
                .map(Files::getFileName)
                .filter(name -> name != null && !name.isEmpty())
                .collect(Collectors.toList());
        // 2. 先删 MinIO 上的对象
        if (!objectNames.isEmpty()) {
            minioUtil.deleteFiles(objectNames);
        }
        // 3. 再删数据库记录
        filesMapper.deleteByIds(fileIds);
    }

    @Override
    public String generatePreviewUrl(String fileId) {
        Files file = filesMapper.selectById(fileId);
        if (file == null || file.getFileName() == null) {
            throw new IllegalArgumentException("文件不存在或未找到对应对象名");
        }
        return minioUtil.getPresignedObjectUrl(file.getFileName());
    }

    @Override
    public List<FileVO> listByBiz(String bizType, String bizId, String fileType) {
        List<Files> files = filesMapper.selectByBiz(bizType, bizId, fileType);
        return files.stream()
                .map(f -> {
                    FileVO vo = new FileVO();
                    BeanUtils.copyProperties(f, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
