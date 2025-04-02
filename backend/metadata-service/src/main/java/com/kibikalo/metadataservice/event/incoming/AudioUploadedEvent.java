package com.kibikalo.metadataservice.event.incoming;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AudioUploadedEvent {
    private String audioId;
    private String rawFilePath;
    private String originalFileName;
    private Instant uploadTimestamp;
}
