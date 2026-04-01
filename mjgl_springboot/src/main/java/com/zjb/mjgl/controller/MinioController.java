package com.zjb.mjgl.controller;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.utils.MinioUtil;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.minio.StatObjectResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/minio")
@PreAuthorize("isAuthenticated()")
@Slf4j
public class MinioController {
    @Autowired
    private MinioUtil minioUtil;

    /**
     * 简单文件上传（使用默认桶）
     *
     * @param file 上传的文件
     * @return 上传结果，包含 fileName、originalFilename、url
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return Result.fail("请选择要上传的文件");
            }
            Map<String, String> result = minioUtil.uploadFile(file);
            if (result == null) {
                return Result.fail("文件上传失败");
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("MinIO单文件上传异常, originalFilename={}", file == null ? null : file.getOriginalFilename(), e);
            return Result.fail("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量文件上传（使用默认桶）
     *
     * @param files 上传的文件列表
     * @return 上传结果列表，每项包含 fileName、originalFilename、url
     */
    @PostMapping("/upload/batch")
    public Result<List<Map<String, String>>> uploadBatch(@RequestParam("files") MultipartFile[] files) {
        try {
            if (files == null || files.length == 0) {
                return Result.fail("请选择要上传的文件");
            }
            List<Map<String, String>> result = minioUtil.uploadFiles(java.util.Arrays.asList(files));
            return Result.success(result);
        } catch (Exception e) {
            log.error("MinIO批量上传异常, count={}", files == null ? 0 : files.length, e);
            return Result.fail("批量上传失败: " + e.getMessage());
        }
    }

    /**
     * 文件下载（使用默认桶）
     *
     * @param fileName 对象名称（Minio 中存储的文件名）
     */
    @GetMapping("/download")
    public void download(@RequestParam("fileName") String fileName, HttpServletResponse response) {
        try {
            if (!minioUtil.objectExists(fileName)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            try (InputStream is = minioUtil.downloadfile(fileName)) {
                StatObjectResponse stat = minioUtil.getObjectStat(fileName);
                String contentType = stat.contentType() != null ? stat.contentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
                String encodedName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
                response.setContentType(contentType);
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName);
                response.setContentLengthLong(stat.size());
                copyStream(is, response.getOutputStream());
                response.flushBuffer();
            }
        } catch (Exception e) {
            log.error("MinIO下载异常, fileName={}", fileName, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 文件预览（使用默认桶，浏览器内打开）
     *
     * @param fileName 对象名称
     */
    @GetMapping("/preview")
    public void preview(@RequestParam("fileName") String fileName, HttpServletResponse response) {
        try {
            if (!minioUtil.objectExists(fileName)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            try (InputStream is = minioUtil.downloadfile(fileName)) {
                StatObjectResponse stat = minioUtil.getObjectStat(fileName);
                String contentType = stat.contentType() != null ? stat.contentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
                response.setContentType(contentType);
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
                response.setContentLengthLong(stat.size());
                copyStream(is, response.getOutputStream());
                response.flushBuffer();
            }
        } catch (Exception e) {
            log.error("MinIO预览异常, fileName={}", fileName, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void copyStream(InputStream in, OutputStream out) throws java.io.IOException {
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) {
            out.write(buf, 0, n);
        }
    }

    /**
     * 删除文件（使用默认桶）
     *
     * @param fileName 对象名称
     */
    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam("fileName") String fileName) {
        try {
            if (!minioUtil.objectExists(fileName)) {
                return Result.fail("文件不存在");
            }
            minioUtil.deleteFile(fileName);
            return Result.success();
        } catch (Exception e) {
            log.error("MinIO删除文件异常, fileName={}", fileName, e);
            return Result.fail("删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 列出默认桶中所有文件
     *
     * @return 文件列表，每项包含 objectName、size、lastModified、url
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        try {
            List<Item> items = minioUtil.listObjects();
            List<Map<String, Object>> list = items.stream()
                    .filter(item -> !item.isDir())
                    .map(item -> {
                        Map<String, Object> map = new java.util.HashMap<>();
                        map.put("objectName", item.objectName());
                        map.put("size", item.size());
                        map.put("lastModified", item.lastModified() != null ? item.lastModified().toString() : null);
                        map.put("url", minioUtil.getObjectUrl(item.objectName(), 7));
                        return map;
                    })
                    .collect(Collectors.toList());
            return Result.success(list);
        } catch (Exception e) {
            log.error("MinIO文件列表查询异常", e);
            return Result.fail("查询文件列表失败: " + e.getMessage());
        }
    }
}
