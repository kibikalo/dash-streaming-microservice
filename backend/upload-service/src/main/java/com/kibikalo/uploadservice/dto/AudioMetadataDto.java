package com.kibikalo.uploadservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AudioMetadataDto {
    private String fileHash;
    private String filePath;
    private String fileFormat;
    private Long fileSize;
    private int duration;
    private String codec;
}
