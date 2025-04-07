package com.kibikalo.shared.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncodingRequestedEvent {
    private String audioId;
    private String rawFilePath;
}
