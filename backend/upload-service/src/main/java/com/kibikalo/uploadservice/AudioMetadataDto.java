package com.kibikalo.uploadservice;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AudioMetadataDto {
    private String fileHash;
    private String fileFormat;
    private long fileSize;
    private int duration;
    private String codec;
}
