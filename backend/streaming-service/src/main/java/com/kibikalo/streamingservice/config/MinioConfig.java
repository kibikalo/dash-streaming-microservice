package com.kibikalo.streamingservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public MinioClient minioClient() {
        log.info("Initializing MinIO client for endpoint: {}", minioUrl);
        try {
            // The endpoint used here influences signature generation
            return MinioClient.builder()
                    .endpoint(minioUrl) // Should be http://localhost:9000
                    .credentials(accessKey, secretKey)
                    .region("us-east-1") // Optional
                    .build();
        } catch (Exception e) {
            log.error("Failed to build MinIO client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize MinIO client", e);
        }
    }
}
