package com.kibikalo.uploadservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class MinioConfig {

    @Value("${app.minio.url}")
    private String minioUrl;

    @Value("${app.minio.access-key}")
    private String accessKey;

    @Value("${app.minio.secret-key}")
    private String secretKey;

    @Value("${app.minio.bucket.raw}")
    private String rawBucketName;

    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
        try {
            // Check if the bucket already exists.
            boolean found = minioClient
                    .bucketExists(BucketExistsArgs.builder().bucket(rawBucketName).build());
            if (!found) {
                // Make a new bucket if not found.
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(rawBucketName).build()
                );
                log.info("MinIO bucket '{}' created successfully.", rawBucketName);
            } else {
                log.info("MinIO bucket '{}' already exists.", rawBucketName);
            }
        } catch (Exception e) {
            log.error("Error interacting with MinIO during startup: {}", e.getMessage(), e);
            // Decide if this should prevent application startup
            // throw new RuntimeException("MinIO initialization failed", e);
        }
        return minioClient;
    }
}
