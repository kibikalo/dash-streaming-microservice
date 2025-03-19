package com.kibikalo.uploadservice.service;

import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.FFprobe;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

@Component
public class AudioValidationUtil {

    private static final Set<String> ALLOWED_AUDIO_FORMATS = Set.of(
            "mp3", "wav", "aac", "ogg", "flac", "mp4"
    );

    private final FFprobe ffprobe;

    public AudioValidationUtil() throws IOException {
        this.ffprobe = new FFprobe("/usr/bin/ffprobe"); // Ensures FFmpeg is installed in the container
    }

    public boolean isValidAudioFile(MultipartFile multipartFile) {
        File tempFile = null;
        try {
            // Convert MultipartFile to File
            tempFile = convertMultipartFileToFile(multipartFile);

            // Validate using FFmpeg
            FFmpegProbeResult probeResult = ffprobe.probe(tempFile.getAbsolutePath());
            String format = probeResult.getFormat().format_name.toLowerCase(); // Extract actual format

            System.out.println(format);

            return ALLOWED_AUDIO_FORMATS.contains(format);
        } catch (IOException e) {
            System.err.println("FFmpeg validation failed: " + e.getMessage());
            return false;
        } finally {
            // Clean up temp file
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("upload_", "_" + multipartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
        }
        return tempFile;
    }
}
