package com.kibikalo.uploadservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudioValidatorTest {

    @InjectMocks
    private AudioValidator audioValidator;

    @Mock
    private AudioFile audioFile;

    @Mock
    private AudioHeader audioHeader;

    @Test
    void validateShouldReturnFailureWhenFileIsEmpty() {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "audio", "test.mp3", "audio/mpeg", new byte[0]);

        // When
        AudioValidationResult result = audioValidator.validate(emptyFile);

        // Then
        assertFalse(result.isValid());
        assertEquals("File is empty or null.", result.getMessage());
    }

    @Test
    void validateShouldReturnFailureWhenFileIsNull() {
        // When
        AudioValidationResult result = audioValidator.validate(null);

        // Then
        assertFalse(result.isValid());
        assertEquals("File is empty or null.", result.getMessage());
    }

    @Test
    void validateShouldReturnSuccessForSupportedFormat() throws Exception {
        // Given
        MockMultipartFile validFile = new MockMultipartFile(
                "audio", "test.mp3", "audio/mpeg", "test-content".getBytes());

        try (MockedStatic<AudioFileIO> audioFileIOMockedStatic = Mockito.mockStatic(AudioFileIO.class)) {
            audioFileIOMockedStatic.when(() -> AudioFileIO.read(any(File.class))).thenReturn(audioFile);
            when(audioFile.getAudioHeader()).thenReturn(audioHeader);
            when(audioHeader.getFormat()).thenReturn("MP3");
            when(audioHeader.getTrackLength()).thenReturn(30); // 30 seconds

            // When
            AudioValidationResult result = audioValidator.validate(validFile);

            // Then
            assertTrue(result.isValid());
            assertEquals("MP3", result.getFormat());
            assertEquals(30, result.getDurationSeconds());
        }
    }

    @Test
    void validateShouldReturnFailureForUnsupportedFormat() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "audio", "test.xyz", "audio/xyz", "test-content".getBytes());

        try (MockedStatic<AudioFileIO> audioFileIOMockedStatic = Mockito.mockStatic(AudioFileIO.class)) {
            audioFileIOMockedStatic.when(() -> AudioFileIO.read(any(File.class))).thenReturn(audioFile);
            when(audioFile.getAudioHeader()).thenReturn(audioHeader);
            when(audioHeader.getFormat()).thenReturn("XYZ"); // Unsupported format

            // When
            AudioValidationResult result = audioValidator.validate(file);

            // Then
            assertFalse(result.isValid());
            assertTrue(result.getMessage().contains("Unsupported audio format"));
        }
    }

    @Test
    void validateShouldReturnFailureForTooShortDuration() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "audio", "test.mp3", "audio/mpeg", "test-content".getBytes());

        try (MockedStatic<AudioFileIO> audioFileIOMockedStatic = Mockito.mockStatic(AudioFileIO.class)) {
            audioFileIOMockedStatic.when(() -> AudioFileIO.read(any(File.class))).thenReturn(audioFile);
            when(audioFile.getAudioHeader()).thenReturn(audioHeader);
            when(audioHeader.getFormat()).thenReturn("MP3");
            when(audioHeader.getTrackLength()).thenReturn(0); // Too short

            // When
            AudioValidationResult result = audioValidator.validate(file);

            // Then
            assertFalse(result.isValid());
            assertTrue(result.getMessage().contains("is less than minimum allowed"));
        }
    }

    @Test
    void validateShouldReturnFailureForTooLongDuration() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "audio", "test.mp3", "audio/mpeg", "test-content".getBytes());

        try (MockedStatic<AudioFileIO> audioFileIOMockedStatic = Mockito.mockStatic(AudioFileIO.class)) {
            audioFileIOMockedStatic.when(() -> AudioFileIO.read(any(File.class))).thenReturn(audioFile);
            when(audioFile.getAudioHeader()).thenReturn(audioHeader);
            when(audioHeader.getFormat()).thenReturn("MP3");
            when(audioHeader.getTrackLength()).thenReturn(60 * 60 * 3); // 3 hours, too long

            // When
            AudioValidationResult result = audioValidator.validate(file);

            // Then
            assertFalse(result.isValid());
            assertTrue(result.getMessage().contains("exceeds maximum allowed"));
        }
    }

    @Test
    void validateShouldHandleCannotReadException() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "audio", "test.mp3", "audio/mpeg", "test-content".getBytes());

        try (MockedStatic<AudioFileIO> audioFileIOMockedStatic = Mockito.mockStatic(AudioFileIO.class)) {
            audioFileIOMockedStatic.when(() -> AudioFileIO.read(any(File.class)))
                    .thenThrow(new CannotReadException("Cannot read file"));

            // When
            AudioValidationResult result = audioValidator.validate(file);

            // Then
            assertFalse(result.isValid());
            assertTrue(result.getMessage().contains("Cannot read audio file"));
        }
    }
}