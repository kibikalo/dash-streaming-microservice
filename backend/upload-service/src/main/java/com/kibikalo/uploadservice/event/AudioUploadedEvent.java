package com.kibikalo.uploadservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AudioUploadedEvent {
    private String audioId;
    private String rawFilePath; // Path within MinIO (object name)
    private String originalFileName;
    private Instant uploadTimestamp;
}
