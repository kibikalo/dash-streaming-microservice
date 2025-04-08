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
        return MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .region("us-east-1")
                .build();
    }
}
