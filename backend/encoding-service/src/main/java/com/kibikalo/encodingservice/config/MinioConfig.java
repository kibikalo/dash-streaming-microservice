package com.kibikalo.encodingservice.config;

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

    @Value("${app.minio.bucket.processed}")
    private String processedBucketName;

    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
        try {
            // Ensure the processed bucket exists
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(processedBucketName).build()
            );
            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(processedBucketName).build()
                );
                log.info(
                        "MinIO bucket '{}' created successfully.",
                        processedBucketName
                );
            } else {
                log.info(
                        "MinIO bucket '{}' already exists.",
                        processedBucketName
                );
            }
        } catch (Exception e) {
            log.error(
                    "Error interacting with MinIO during startup for bucket '{}': {}",
                    processedBucketName,
                    e.getMessage(),
                    e
            );
            // Consider if this should prevent startup
        }
        return minioClient;
    }
}