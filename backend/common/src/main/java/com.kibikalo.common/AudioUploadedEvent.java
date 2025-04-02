package com.kibikalo.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AudioUploadedEvent {
    private String audioId;
    private String rawFilePath;
    private String originalFileName;
    private Instant uploadTimestamp;
}

