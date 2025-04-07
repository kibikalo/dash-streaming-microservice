package com.kibikalo.uploadservice.service;

import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

import com.kibikalo.shared.events.AudioUploadedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadService {

    private final MinioClient minioClient;
    private final KafkaTemplate<String, AudioUploadedEvent> kafkaTemplate;

    @Value("${app.minio.bucket.raw}")
    private String rawBucketName;

    @Value("${app.kafka.topic.audio-uploaded}")
    private String audioUploadedTopic;

    public String uploadAudio(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        String originalFileName = StringUtils.cleanPath(
                file.getOriginalFilename() != null
                        ? file.getOriginalFilename()
                        : "unknown"
        );
        String audioId = UUID.randomUUID().toString();
        // Define path structure in MinIO: raw-audio/{audioId}/{originalFileName}
        String objectName = String.format("raw-audio/%s/%s", audioId, originalFileName);

        try (InputStream inputStream = file.getInputStream()) {
            log.info(
                    "Uploading file '{}' to MinIO bucket '{}' as '{}'",
                    originalFileName,
                    rawBucketName,
                    objectName
            );

            // Upload file to MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(rawBucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1) // -1 part size for auto
                            .contentType(file.getContentType()) // Store content type
                            .build()
            );

            log.info("File uploaded successfully to MinIO: {}", objectName);

            // Create event payload
            AudioUploadedEvent event = new AudioUploadedEvent(
                    audioId,
                    objectName, // Use the MinIO object path
                    originalFileName,
                    Instant.now()
            );

            // Publish event to Kafka
            // Use audioId as the key for partitioning (optional but good practice)
            kafkaTemplate.send(audioUploadedTopic, audioId, event);
            log.info(
                    "Published AudioUploadedEvent for audioId: {} to topic: {}",
                    audioId,
                    audioUploadedTopic
            );

            return audioId; // Return the generated ID

        } catch (Exception e) {
            log.error(
                    "Error uploading file '{}' or publishing event: {}",
                    originalFileName,
                    e.getMessage(),
                    e
            );
            // Consider specific exception handling (MinIO vs Kafka vs IO)
            // You might want to implement cleanup logic here (e.g., delete from MinIO if Kafka fails)
            throw new RuntimeException("Failed to process audio upload", e);
        }
    }
}