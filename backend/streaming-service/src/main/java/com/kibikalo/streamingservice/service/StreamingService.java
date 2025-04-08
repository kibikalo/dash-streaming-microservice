package com.kibikalo.streamingservice.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kibikalo.shared.dto.AudioMetadataDto;
import com.kibikalo.shared.model.AudioStatus; // Assuming shared enum
import com.kibikalo.streamingservice.config.MetadataServiceClient;
import com.kibikalo.streamingservice.exception.ResourceNotFoundException;
import com.kibikalo.streamingservice.exception.ResourceNotReadyException;

import feign.FeignException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamingService {

    private final MetadataServiceClient metadataServiceClient;
    private final MinioClient minioClient;

    @Value("${app.minio.bucket.processed}")
    private String processedBucket;

    @Value("${app.minio.presigned-url-expiry-seconds}")
    private int presignedUrlExpirySeconds;

    public String getManifestUrl(String audioId) {
        log.info("Request received for manifest URL for audioId: {}", audioId);

        AudioMetadataDto metadata;
        try {
            log.debug("Querying metadata service for audioId: {}", audioId);
            metadata = metadataServiceClient.getMetadata(audioId);
            log.debug("Received metadata: Status={}, Path={}", metadata.getStatus(), metadata.getManifestPath());
        } catch (FeignException.NotFound e) {
            log.warn("Metadata not found for audioId: {}", audioId);
            throw new ResourceNotFoundException("Audio metadata not found for ID: " + audioId);
        } catch (Exception e) {
            log.error("Error calling metadata service for audioId {}: {}", audioId, e.getMessage(), e);
            // Treat other errors as temporary unavailability
            throw new ResourceNotReadyException("Could not retrieve metadata for ID: " + audioId);
        }

        if (metadata.getStatus() != AudioStatus.AVAILABLE) {
            log.warn("AudioId {} is not available for streaming. Status: {}", audioId, metadata.getStatus());
            throw new ResourceNotReadyException("Audio is not yet available for streaming. Status: " + metadata.getStatus());
        }

        if (metadata.getManifestPath() == null || metadata.getManifestPath().isBlank()) {
            log.error("AudioId {} is AVAILABLE but manifest path is missing!", audioId);
            throw new ResourceNotReadyException("Manifest path missing for available audio: " + audioId);
        }

        // Generate pre-signed URL for the manifest path
        try {
            String presignedUrl = generatePresignedUrl(metadata.getManifestPath());
            log.info("Generated pre-signed URL for audioId {}: {}", audioId, presignedUrl);
            return presignedUrl;
        } catch (Exception e) {
            log.error("Failed to generate pre-signed URL for audioId {} path {}: {}",
                    audioId, metadata.getManifestPath(), e.getMessage(), e);
            throw new RuntimeException("Failed to prepare streaming URL for ID: " + audioId);
        }

        // Construct simple public URL
//        String baseUrl = minioExternalBaseUrl.endsWith("/") ? minioExternalBaseUrl : minioExternalBaseUrl + "/";
//        String bucket = processedBucket + "/";
//        String objectPath = metadata.getManifestPath().startsWith("/") ? metadata.getManifestPath().substring(1) : metadata.getManifestPath();
//
//        String directUrl = baseUrl + bucket + objectPath;
//        log.info("Constructed direct public URL for audioId {}: {}", audioId, directUrl);
//        return directUrl;
    }

    private String generatePresignedUrl(String objectName) throws Exception {
        log.debug("Generating pre-signed URL for object: {} in bucket: {}", objectName, processedBucket);
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(processedBucket)
                        .object(objectName)
                        .expiry(presignedUrlExpirySeconds, TimeUnit.SECONDS)
                        .build());
    }
}