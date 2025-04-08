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

        // --- Define RELATIVE object name ---
        String relativeObjectName = String.format("%s/%s", audioId, originalFileName);
// ---------------------------------

        try (InputStream inputStream = file.getInputStream()) {
            log.info(
                    "Uploading file '{}' to MinIO bucket '{}' as '{}'",
                    originalFileName,
                    rawBucketName, // Use the bucket name variable
                    relativeObjectName // Use the relative object name
            );

            // Upload file to MinIO
            log.info("--- UPLOAD DEBUG ---");
            log.info("Using Bucket Name: '{}'", rawBucketName); // Log the bucket name value
            log.info("Using Object Key : '{}'", relativeObjectName); // Log the object key value
            log.info("Target Full Path (Expected): {}/{}", rawBucketName, relativeObjectName);
            log.info("Uploading file '{}' to MinIO...", originalFileName);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(rawBucketName) // Specify bucket
                            .object(relativeObjectName) // Specify RELATIVE object key
                            .stream(inputStream, file.getSize(), -1) // -1 part size for auto
                            .contentType(file.getContentType()) // Store content type
                            .build()
            );

            log.info("File uploaded successfully to MinIO: {}", relativeObjectName);

            // Create event payload (use the RELATIVE path)
            AudioUploadedEvent event = new AudioUploadedEvent(
                    audioId,
                    relativeObjectName, // Use the relative path for the event
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