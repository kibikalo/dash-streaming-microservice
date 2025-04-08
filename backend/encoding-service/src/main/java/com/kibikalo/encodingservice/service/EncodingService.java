package com.kibikalo.encodingservice.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import com.kibikalo.shared.events.EncodingFailedEvent;
import com.kibikalo.shared.events.EncodingRequestedEvent;
import com.kibikalo.shared.events.EncodingSucceededEvent;
import io.minio.GetObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EncodingService {

    private final MinioClient minioClient;
    private final FFmpegService ffmpegService;
    private final KafkaTemplate<String, Object> kafkaTemplate; // Use Object for multiple event types

    @Value("${app.minio.bucket.raw}")
    private String rawBucket;

    @Value("${app.minio.bucket.processed}")
    private String processedBucket;

    @Value("${app.kafka.topic.encoding-succeeded}")
    private String successTopic;

    @Value("${app.kafka.topic.encoding-failed}")
    private String failedTopic;

    @Value("${app.encoding.bitrates-kbps}")
    private String bitratesConfig; // Comma-separated e.g., "64,128"

    @Value("${app.encoding.segment-duration-seconds}")
    private int segmentDuration;

    @Value("${app.encoding.codec}")
    private String targetCodec;

    public void processEncodingRequest(EncodingRequestedEvent event) {
        String audioId = event.getAudioId();
        String rawFilePath = event.getRawFilePath();
        log.info(
                "Processing encoding request for audioId: {}, rawPath: {}",
                audioId,
                rawFilePath
        );

        Path tempInputFile = null;
        Path tempOutputDir = null;
        StatObjectResponse rawFileStats = null;

        try {
            // --- Get Raw File Stats from MinIO ---
            try {
                log.info("Fetching stats for {} from bucket {}", rawFilePath, rawBucket);
                rawFileStats = minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(rawBucket)
                                .object(rawFilePath)
                                .build()
                );
                log.info("Got stats: Size={}, ContentType={}", rawFileStats.size(), rawFileStats.contentType());
            } catch (Exception e) {
                log.error("Failed to get stats for raw file {} from MinIO: {}", rawFilePath, e.getMessage());
                // Decide if this is a fatal error for the encoding process
                throw new RuntimeException("Cannot get stats for raw file: " + rawFilePath, e);
            }
            // ---------------------------------------

            // 1. Create temporary local directories/files
            tempOutputDir = Files.createTempDirectory("encode-out-" + audioId + "-");
            // Extract original filename to use for temp file (optional)
            String originalFileName = extractFileName(rawFilePath);
            tempInputFile = tempOutputDir.resolve(originalFileName); // Place input inside output dir temporarily

            log.debug(
                    "Created temp output dir: {}",
                    tempOutputDir.toAbsolutePath()
            );
            log.debug(
                    "Temporary input file path: {}",
                    tempInputFile.toAbsolutePath()
            );

            // 2. Download raw file from MinIO
            log.info("Downloading {} from bucket {}", rawFilePath, rawBucket);
            downloadFromMinio(rawBucket, rawFilePath, tempInputFile);
            log.info("Downloaded raw file to {}", tempInputFile);

            // 3. Perform DASH encoding using FFmpeg
            List<Integer> bitrates = parseBitrates(bitratesConfig);
            String manifestName = "manifest.mpd"; // Standard name

            boolean encodingSuccess = ffmpegService.runDashEncoding(
                    tempInputFile,
                    tempOutputDir,
                    manifestName,
                    bitrates,
                    segmentDuration,
                    targetCodec
            );

            if (!encodingSuccess) {
                throw new RuntimeException("FFmpeg encoding failed.");
            }

            log.info("FFmpeg encoding completed successfully for {}", audioId);

            // 4. Upload processed files (manifest + segments) to MinIO
            String relativeBaseDir = audioId + "/";
            String minioUploadPrefix = "processed-audio/" + relativeBaseDir;
            uploadDirectoryToMinio(tempOutputDir, processedBucket, minioUploadPrefix, tempInputFile);
            log.info(
                    "Uploaded encoded files to MinIO bucket '{}' prefix '{}'",
                    processedBucket,
                    minioUploadPrefix
            );

            // 5. Publish Success Event
            String relativeManifestPath = relativeBaseDir + manifestName;
            EncodingSucceededEvent successEvent = new EncodingSucceededEvent(
                    audioId,
                    relativeManifestPath, // Send relative path
                    relativeBaseDir, // Base path for segments
                    ffmpegService.getLastDurationMillis(), // Get duration parsed by FFmpegService
                    bitrates,
                    targetCodec,
                    Instant.now(),
                    rawFileStats.size(),
                    rawFileStats.contentType()
            );
            publishEvent(successTopic, audioId, successEvent);
            log.info(
                    "Published EncodingSucceededEvent for audioId: {}",
                    audioId
            );

        } catch (Exception e) {
            log.error(
                    "Encoding process failed for audioId {}: {}",
                    audioId,
                    e.getMessage(),
                    e
            );
            // 6. Publish Failure Event
            EncodingFailedEvent failedEvent = new EncodingFailedEvent(
                    audioId,
                    e.getMessage(),
                    Instant.now()
            );
            publishEvent(failedTopic, audioId, failedEvent);
            log.info("Published EncodingFailedEvent for audioId: {}", audioId);

        } finally {
            // 7. Cleanup temporary files/directories
            cleanupTempFiles(tempInputFile, tempOutputDir);
        }
    }

    private void downloadFromMinio(
            String bucket,
            String objectName,
            Path destination
    ) throws IOException {
        try (
                InputStream stream = minioClient.getObject(
                        GetObjectArgs.builder()             // <-- Use GetObjectArgs
                                .bucket(bucket)
                                .object(objectName)
                                .build()
                )
        ) {
            Files.copy(stream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (MinioException | java.security.InvalidKeyException | java.security.NoSuchAlgorithmException e) {
            throw new IOException(
                    "Failed to download " + objectName + " from MinIO: " + e.getMessage(),
                    e
            );
        }
    }

    private void uploadDirectoryToMinio(
            Path sourceDirectory,
            String bucket,
            String fullUploadPrefix,
            Path tempInputFileToExclude
    ) throws IOException {
        try (Stream<Path> stream = Files.walk(sourceDirectory, 1)) { // Walk only top-level files
            List<Path> filesToUpload = stream.filter(Files::isRegularFile)
                    // Exclude the original input file path
                    .filter(path -> !path.equals(tempInputFileToExclude))
                    .collect(Collectors.toList());

            if (filesToUpload.isEmpty()) {
                log.warn("No files found in {} to upload.", sourceDirectory);
                // This might indicate an encoding issue if manifest/segments are missing
                // TODO: throw new IOException("No output files generated by FFmpeg.");
            }

            for (Path filePath : filesToUpload) {
                String objectName = fullUploadPrefix + filePath.getFileName().toString();
                log.debug(
                        "Uploading {} to MinIO as {}",
                        filePath.getFileName(),
                        objectName
                );
                try {
                    minioClient.uploadObject(
                            UploadObjectArgs.builder()
                                    .bucket(bucket)
                                    .object(objectName)
                                    .filename(filePath.toAbsolutePath().toString())
                                    // .contentType(...) // Optional: set content type if known
                                    .build()
                    );
                } catch (MinioException | java.security.InvalidKeyException | java.security.NoSuchAlgorithmException e) {
                    throw new IOException(
                            "Failed to upload " + filePath.getFileName() + " to MinIO: " + e.getMessage(),
                            e
                    );
                }
            }
        }
    }

    private void publishEvent(String topic, String key, Object event) {
        try {
            kafkaTemplate.send(topic, key, event);
        } catch (Exception e) {
            // Log Kafka publishing errors, but don't let them stop cleanup
            log.error(
                    "Failed to publish event to Kafka topic {}: {}",
                    topic,
                    e.getMessage(),
                    e
            );
        }
    }

    private void cleanupTempFiles(Path inputFile, Path outputDir) {
        try {
            if (inputFile != null && Files.exists(inputFile)) {
                Files.delete(inputFile);
                log.debug("Deleted temp input file: {}", inputFile);
            }
            if (outputDir != null && Files.exists(outputDir)) {
                // Delete segments and manifest first
                try (Stream<Path> walk = Files.walk(outputDir)) {
                    walk.sorted(java.util.Comparator.reverseOrder()) // Delete files before dirs
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                    log.debug("Deleted temp file/dir: {}", path);
                                } catch (IOException e) {
                                    log.error(
                                            "Failed to delete temp file/dir: {}",
                                            path,
                                            e
                                    );
                                }
                            });
                }
                // Now delete the top-level temp dir (should be empty)
                // Files.delete(outputDir); // Already deleted by walk
                log.debug("Deleted temp output directory: {}", outputDir);
            }
        } catch (IOException e) {
            log.warn("Error cleaning up temporary files: {}", e.getMessage());
        }
    }

    private List<Integer> parseBitrates(String config) {
        return Arrays.stream(config.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    // Helper to get filename from MinIO path
    private String extractFileName(String path) {
        if (path == null) return "unknown_" + UUID.randomUUID();
        String cleanedPath = StringUtils.cleanPath(path);
        int lastSlash = cleanedPath.lastIndexOf('/');
        return (lastSlash >= 0)
                ? cleanedPath.substring(lastSlash + 1)
                : cleanedPath;
    }
}