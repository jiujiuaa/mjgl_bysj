package com.zjb.mjgl.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileVO {
    private String id;
    private String fileType;      // "photo", "drawing"...
    private String originalName;
    private String filePath;      // 前端用于拼接下载 URL
    private Long fileSize;
    private LocalDateTime uploadTime;
    private String description;


}