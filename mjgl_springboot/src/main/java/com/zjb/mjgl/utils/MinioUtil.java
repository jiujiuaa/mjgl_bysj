package com.zjb.mjgl.utils;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class MinioUtil {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String defaultBucketName;

    /**
    * 判断桶是否存在
    **/
    @SneakyThrows
    public boolean bucketExists(String bucketName) {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    /**
     * 创建桶
     * **/
    @SneakyThrows
    public void makeBucket(String bucketName) {
        if(!bucketExists(bucketName)){
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }
    /**
     * 获取桶列表
     * **/
    @SneakyThrows
    public List<Bucket> ListBuckets() {
        return minioClient.listBuckets();
    }
    /**
     * 删除桶
     * **/
    @SneakyThrows
    public void removeBucket(String bucketName) {
        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }

    /**
     *
     * @param file 文件
     * @param bucketName 桶名称
     * @return 文件信息
     */
    @SneakyThrows
    public Map<String,String> uploadFile(MultipartFile file,String bucketName) {
        if(file == null || file.isEmpty()){
            return null;
        }
        if(!bucketExists(bucketName)){
            makeBucket(bucketName);
        }
        String originalFilename = file.getOriginalFilename();
        String fileName =IdUtil.fastUUID()+originalFilename.substring(originalFilename.lastIndexOf("."));
        long fileSize = file.getSize();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .contentType(file.getContentType())
                        .stream(file.getInputStream(), file.getSize(),-1)
                        .build());
        HashMap<String, String> resultMap = new HashMap<>();
        resultMap.put("fileName",fileName);
        resultMap.put("originalFilename",originalFilename);
        resultMap.put("fileSize", String.valueOf(fileSize));
        resultMap.put("filePath", getObjectUrl(bucketName, fileName, 7));
        return resultMap;
    }

    /**
     * 简单文件上传
     * @param file
     * @return 文件信息
     */
    public Map<String,String> uploadFile(MultipartFile file) {
        return uploadFile(file,defaultBucketName);
    }

    /**
     * 批量文件上传
     * @param files 文件列表
     * @param bucketName 桶名称
     * @return 文件信息列表
     */
    public List<Map<String,String>> uploadFiles(List<MultipartFile> files,String bucketName) {
        return files.stream()
                .map(file->uploadFile(file,bucketName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 批量上传使用默认桶
     * @param files 文件列表
     * @return 文件信息列表
     */
    public List<Map<String,String>> uploadFiles(List<MultipartFile> files) {
        return uploadFiles(files,defaultBucketName);
    }

    /**
     * 下载文件
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @return  输入流
     */
    @SneakyThrows
    public InputStream downloadFile(String bucketName, String objectName) {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }

    /**
     * 下载文件 使用默认桶
     * @param objectName 对象名称
     * @return  输入流
     */
    public InputStream downloadfile( String objectName) {
        return downloadFile(defaultBucketName, objectName);
    }

    /**
     * 删除文件
     * @param bucketName 桶名称
     * @param objectName 对象名称
     */
    @SneakyThrows
    public void deleteFile(String bucketName, String objectName) {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }
    /**
     * 删除文件（使用默认存储桶）
     * @param objectName 对象名称
     */
    @SneakyThrows
    public void deleteFile(String objectName) {
        deleteFile(defaultBucketName, objectName);
    }

    /**
     * 获取对象元数据
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @return 对象元数据（含 contentType、size 等）
     */
    @SneakyThrows
    public io.minio.StatObjectResponse getObjectStat(String bucketName, String objectName) {
        return minioClient.statObject(StatObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }

    /**
     * 获取对象元数据（使用默认存储桶）
     * @param objectName 对象名称
     * @return 对象元数据
     */
    @SneakyThrows
    public io.minio.StatObjectResponse getObjectStat(String objectName) {
        return getObjectStat(defaultBucketName, objectName);
    }

    /**
     * 批量文件删除
     * @param bucketName 桶名称
     * @param objectNames 对象名称列表
     * @return  删除错误列表
     */
    @SneakyThrows
    public List<DeleteError> deleteFiles(String bucketName,List<String> objectNames) {
        List<DeleteObject> objects = objectNames.stream().map(DeleteObject::new).collect(Collectors.toList());
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                .bucket(bucketName)
                .objects(objects)
                .build());
        List<DeleteError> deleteErrors = new ArrayList<>();
        //results.forEach(result -> deleteErrors.add(result.get()));
        for (Result<DeleteError> result : results) {
            deleteErrors.add(result.get());
        }
        return deleteErrors;
    }

    /**
     * 批量删除文件（使用默认存储桶）
     * @param objectNames 对象名称列表
     * @return 删除错误列表
     */
    public List<DeleteError> deleteFiles(List<String> objectNames) {
        return deleteFiles(defaultBucketName, objectNames);
    }

    /**
     * 获取文件URL
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expires    过期时间（天）
     * @return 文件URL
     */
    @SneakyThrows
    public String getObjectUrl(String bucketName, String objectName, int expires) {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(objectName)
                .expiry(expires, TimeUnit.DAYS)
                .build());
    }


    /**
     * 获取文件URL（使用默认存储桶）
     * @param objectName 对象名称
     * @param expires    过期时间（天）
     * @return 文件URL
     */
    public String getObjectUrl(String objectName, int expires) {
        return getObjectUrl(defaultBucketName, objectName, expires);
    }

    /**
     * 检查文件是否存在
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 是否存在
     */
    @SneakyThrows
    public boolean objectExists(String bucketName, String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查文件是否存在（使用默认存储桶）
     * @param objectName 对象名称
     * @return 是否存在
     */
    public boolean objectExists(String objectName) {
        return objectExists(defaultBucketName, objectName);
    }

    /**
     * 列出存储桶中的所有对象
     * @param bucketName 存储桶名称
     * @return 对象列表
     */
    @SneakyThrows
    public List<Item> listObjects(String bucketName) {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .build());

        List<Item> items = new ArrayList<>();
        for (Result<Item> result : results) {
            items.add(result.get());
        }
        return items;
    }

    /**
     * 列出存储桶中的所有对象（使用默认存储桶）
     * @return 对象列表
     */
    public List<Item> listObjects() {
        return listObjects(defaultBucketName);
    }

    /**
     * 上传字节流文件到 MinIO（用于 PDF 等非 MultipartFile 场景）。
     *
     * @param bytes 文件字节数组
     * @param objectName MinIO 对象名（可包含目录）
     * @param contentType 内容类型，例：application/pdf
     */
    @SneakyThrows
    public Map<String, String> uploadBytes(byte[] bytes, String objectName, String contentType) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        if (objectName == null || objectName.trim().isEmpty()) {
            return null;
        }
        if (!bucketExists(defaultBucketName)) {
            makeBucket(defaultBucketName);
        }

        String effectiveContentType = (contentType == null || contentType.trim().isEmpty())
                ? "application/octet-stream"
                : contentType;

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(defaultBucketName)
                        .object(objectName)
                        .contentType(effectiveContentType)
                        .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                        .build()
        );

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("fileName", objectName);
        resultMap.put("originalFilename", objectName);
        resultMap.put("fileSize", String.valueOf(bytes.length));
        // 这里仅给出短期预览 URL（现有系统对 filePath 的语义即为“可下载 URL”）
        resultMap.put("filePath", getObjectUrl(objectName, 7));
        return resultMap;
    }


    /**
     * 生成文件哈希值（用于秒传判断）
     * @param file 文件
     * @return 哈希值
     */
    @SneakyThrows
    public String generateFileHash(MultipartFile file) {
        // 这里使用简单的文件大小和修改时间作为哈希值，实际应用中应使用MD5或SHA-1等算法
        return file.getSize() + "-" + file.getOriginalFilename();
    }



}
