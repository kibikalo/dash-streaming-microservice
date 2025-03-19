package com.kibikalo.metadataservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetadataDto {
    private String fileHash;
    private String filePath;
    private String fileFormat;
    private Long fileSize;
    private int duration;
    private String codec;
}
