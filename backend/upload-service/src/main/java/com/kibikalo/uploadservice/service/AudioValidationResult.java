package com.kibikalo.uploadservice.service;

import lombok.Getter;

@Getter
public class AudioValidationResult {
    private final boolean valid;
    private final String message;
    private final String format;
    private final Integer durationSeconds;

    private AudioValidationResult(boolean valid, String message, String format, Integer durationSeconds) {
        this.valid = valid;
        this.message = message;
        this.format = format;
        this.durationSeconds = durationSeconds;
    }

    public static AudioValidationResult success(String format, int durationSeconds) {
        return new AudioValidationResult(true, "Audio format is supported.", format, durationSeconds);
    }

    public static AudioValidationResult failure(String message) {
        return new AudioValidationResult(false, message, null, null);
    }
}