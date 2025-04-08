package com.kibikalo.shared.dto;

import com.kibikalo.shared.model.AudioStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AudioMetadataDto {
    private String id;
    private AudioStatus status;
    private String manifestPath;
}
