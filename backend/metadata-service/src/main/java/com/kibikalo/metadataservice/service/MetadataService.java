package com.kibikalo.metadataservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kibikalo.metadataservice.exception.MetadataNotFoundException;
import com.kibikalo.metadataservice.model.AudioMetadata;
import com.kibikalo.metadataservice.repo.AudioMetadataRepository;
import com.kibikalo.shared.dto.AudioMetadataDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetadataService {

    private final AudioMetadataRepository metadataRepository;

    @Transactional(readOnly = true)
    public AudioMetadataDto getAudioMetadataById(String audioId) {
        log.debug("Attempting to find metadata for audioId: {}", audioId);

        AudioMetadata metadata = metadataRepository.findById(audioId)
                .orElseThrow(() -> {
                    log.warn("Metadata not found for audioId: {}", audioId);
                    return new MetadataNotFoundException("Metadata not found for audio ID: " + audioId);
                });

        log.info("Found metadata for audioId: {}", audioId);

        return mapToDTO(metadata);
    }

    private AudioMetadataDto mapToDTO(AudioMetadata entity) {
        if (entity == null) {
            return null;
        }
        return new AudioMetadataDto(
                entity.getId(),
                entity.getStatus(),
                entity.getManifestPath()
        );
    }
}