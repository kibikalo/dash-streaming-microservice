package com.kibikalo.shared.events;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncodingFailedEvent {
    private String audioId;
    private String errorMessage;
    private Instant failureTimestamp;
}
