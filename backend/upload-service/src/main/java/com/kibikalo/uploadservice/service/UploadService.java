package com.kibikalo.uploadservice.service;

import com.kibikalo.uploadservice.dto.AudioMetadataDto;
import lombok.AllArgsConstructor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

@Service
@AllArgsConstructor
public class UploadService {

    private static final Set<String> ALLOWED_AUDIO_FORMATS = Set.of("audio/mpeg", "audio/wav", "audio/mp4", "audio/aac", "audio/ogg", "audio/flac");
    private final AudioValidationUtil audioValidationUtil;
    private final MetadataServiceClient metadataServiceClient;

    // Validate if file is an allowed audio type
    public boolean isValidAudioFile(MultipartFile file) throws IOException {
        return audioValidationUtil.isValidAudioFile(file);
    }

    // Process the file: generate hash, extract metadata
    public AudioMetadataDto processFile(MultipartFile file) throws IOException {
        // Generate unique file hash
        String fileHash = generateFileHash(file);

        // Save the file temporarily to extract metadata
        File tempFile = File.createTempFile("audio_", null);
        file.transferTo(tempFile);

        // Extract metadata
        AudioMetadataDto metadata = extractMetadata(fileHash, file, tempFile);

        // Delete temp file
        Files.delete(tempFile.toPath());

        // Send metadata to metadata-service
        metadataServiceClient.saveMetadata(metadata.getFileHash(), metadata.getFilePath(), metadata.getFileFormat(), metadata.getFileSize(), metadata.getDuration(), metadata.getCodec());

        return metadata;
    }

    // Generate a unique hash for the file
    private String generateFileHash(MultipartFile file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = file.getBytes();
            byte[] hashBytes = digest.digest(fileBytes);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(Integer.toHexString(0xFF & b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Hashing algorithm not found", e);
        }
    }

    // Extract audio metadata
    public AudioMetadataDto extractMetadata(String fileHash, MultipartFile file, File tempFile) {
        try {
            FFprobe ffprobe = new FFprobe("/usr/bin/ffprobe");
            FFmpegProbeResult result = ffprobe.probe(tempFile.getAbsolutePath());

            FFmpegFormat format = result.getFormat();
            String fileFormat = file.getContentType();
            Long fileSize = format.size;
            int duration = (int) format.duration;
            String codec = format.format_name;

            return new AudioMetadataDto(fileHash, "test/test", fileFormat, fileSize, duration, codec);
        } catch (Exception e) {
            System.err.println("Failed to extract metadata: " + e.getMessage());
            return new AudioMetadataDto(fileHash, "test/test", file.getContentType(), file.getSize(), 0, "Unknown");
        }
    }
}
