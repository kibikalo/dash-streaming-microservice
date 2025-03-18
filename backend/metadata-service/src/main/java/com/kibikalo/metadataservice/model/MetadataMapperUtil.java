package com.kibikalo.metadataservice.model;

import org.springframework.stereotype.Component;

@Component
public class MetadataMapperUtil {

    public MetadataDto toDto(Metadata metadata) {
        return new MetadataDto(
                metadata.getFileHash(),
                metadata.getFilePath(),
                metadata.getFileFormat(),
                metadata.getFileSize(),
                metadata.getDuration(),
                metadata.getCodec()
        );
    }

    public Metadata toEntity(MetadataDto metadataDto) {
        Metadata metadata = new Metadata();
        metadata.setFileHash(metadataDto.getFileHash());
        metadata.setFilePath(metadataDto.getFilePath());
        metadata.setFileFormat(metadataDto.getFileFormat());
        metadata.setFileSize(metadataDto.getFileSize());
        metadata.setDuration(metadataDto.getDuration());
        metadata.setCodec(metadataDto.getCodec());
        return metadata;
    }
}

