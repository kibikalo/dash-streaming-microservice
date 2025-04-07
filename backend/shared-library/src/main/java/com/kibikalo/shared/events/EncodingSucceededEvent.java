package com.kibikalo.shared.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncodingSucceededEvent {
    private String audioId;
    private String manifestPath; // e.g., processed-audio/{audioId}/manifest.mpd
    private String segmentBasePath; // e.g., processed-audio/{audioId}/
    private Long durationMillis; // Extracted during encoding
    private List<Integer> bitratesKbps; // e.g., [64, 128]
    private String codec; // e.g., "aac"
    private Instant encodingTimestamp;
    private Long rawFileSize; // Size in bytes
    private String rawFileFormat; // Content type (e.g., "audio/wav")
}
