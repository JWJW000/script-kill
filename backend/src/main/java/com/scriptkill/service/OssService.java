package com.scriptkill.service;

import com.scriptkill.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OssService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    };

    /**
     * 上传文件到 MinIO
     *
     * @param file     上传的文件
     * @param category 分类目录，如 avatar / cover
     * @return 文件的访问 URL
     */
    public String upload(MultipartFile file, String category) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 按 分类/日期/UUID 组织路径，类似若依Plus的做法
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String objectName = category + "/" + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + extension;

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            // 拼接完整访问 URL（公共读，直接拼接即可）
            String url = minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + objectName;
            log.info("File uploaded: {}", url);
            return url;

        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件的完整 URL
     */
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            // 从 URL 中提取 objectName
            String prefix = minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/";
            if (fileUrl.startsWith(prefix)) {
                String objectName = fileUrl.substring(prefix.length());
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .object(objectName)
                                .build());
                log.info("File deleted: {}", objectName);
            }
        } catch (Exception e) {
            log.warn("File delete failed: {}", e.getMessage());
        }
    }

    /**
     * 获取临时访问URL（用于私有桶场景，当前项目用公共读所以一般不需要）
     */
    public String getPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(2, TimeUnit.HOURS)
                            .build());
        } catch (Exception e) {
            log.error("Get presigned url failed", e);
            throw new RuntimeException("获取文件URL失败");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小不能超过10MB");
        }
        String contentType = file.getContentType();
        boolean allowed = false;
        if (contentType != null) {
            for (String type : ALLOWED_IMAGE_TYPES) {
                if (type.equalsIgnoreCase(contentType)) {
                    allowed = true;
                    break;
                }
            }
        }
        if (!allowed) {
            throw new RuntimeException("只支持 JPG/PNG/GIF/WebP/BMP 格式的图片");
        }
    }
}
