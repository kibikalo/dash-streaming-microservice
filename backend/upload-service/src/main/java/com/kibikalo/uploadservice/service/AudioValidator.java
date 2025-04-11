package com.kibikalo.uploadservice.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AudioValidator {

    private static final Set<String> SUPPORTED_FORMATS = new HashSet<>(Arrays.asList(
            "Wave", "Wav", "MP3", "FLAC", "Ogg Vorbis", "MP4"
    ));

    private static final int MIN_DURATION_SECONDS = 1;
    private static final int MAX_DURATION_SECONDS = 60 * 60 * 2; // max 2 hours

    public AudioValidationResult validate(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return AudioValidationResult.failure("File is empty or null.");
        }

        File tempFile = null;
        try {
            // 1. Convert to temporary file for JAudioTagger
            tempFile = convertMultiPartToFile(multipartFile);
            log.debug("Created temporary file for validation: {}", tempFile.getAbsolutePath());

            // 2. Read audio file information
            AudioFile audioFile = AudioFileIO.read(tempFile);
            AudioHeader header = audioFile.getAudioHeader();

            if (header == null) {
                return AudioValidationResult.failure("Could not read audio header. Invalid or unsupported file structure.");
            }

            // 3. Check Format
            String format = header.getFormat();
            log.info("Detected audio format: {}", format);
            if (!SUPPORTED_FORMATS.contains(format)) {
                return AudioValidationResult.failure("Unsupported audio format: " + format + ". Supported formats are: " + SUPPORTED_FORMATS);
            }

            // 4. Check Duration
            int durationSeconds = header.getTrackLength();
            log.info("Detected duration: {} seconds", durationSeconds);
            if (durationSeconds < MIN_DURATION_SECONDS) {
                return AudioValidationResult.failure("Audio duration (" + durationSeconds + "s) is less than minimum allowed (" + MIN_DURATION_SECONDS + "s).");
            }
            if (durationSeconds > MAX_DURATION_SECONDS) {
                return AudioValidationResult.failure("Audio duration (" + durationSeconds + "s) exceeds maximum allowed (" + MAX_DURATION_SECONDS + "s).");
            }

            log.info("Audio file validation successful for format: {}, duration: {}s", format, durationSeconds);
            return AudioValidationResult.success(format, durationSeconds);

        } catch (CannotReadException e) {
            log.warn("JAudioTagger cannot read file [{}]: {}", multipartFile.getOriginalFilename(), e.getMessage());
            return AudioValidationResult.failure("Cannot read audio file. It might be corrupted, not a supported audio format, or not an audio file at all.");
        } catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            log.error("Error during audio validation for file [{}]: {}", multipartFile.getOriginalFilename(), e.getMessage(), e);
            return AudioValidationResult.failure("Error processing audio file during validation: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during audio validation for file [{}]: {}", multipartFile.getOriginalFilename(), e.getMessage(), e);
            return AudioValidationResult.failure("An unexpected error occurred during validation.");
        } finally {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    log.warn("Could not delete temporary validation file: {}", tempFile.getAbsolutePath());
                } else {
                    log.debug("Deleted temporary validation file: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "temp_audio";
        // Sanitize filename slightly for temp file creation
        String safeSuffix = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        File convFile = File.createTempFile("validate_", "_" + safeSuffix);
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }
}