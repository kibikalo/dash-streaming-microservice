package com.kibikalo.metadataservice.service;

import com.kibikalo.metadataservice.model.FileStatus;
import com.kibikalo.metadataservice.model.Metadata;
import com.kibikalo.metadataservice.repo.MetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
//@RequiredArgsConstructor
public class MetadataService {

    private final MetadataRepository metadataRepository;

    public MetadataService(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    public Metadata saveMetadata(Metadata metadata) {
        return metadataRepository.save(metadata);
    }

    public Optional<Metadata> getMetadataById(Long id) {
        return metadataRepository.findById(id);
    }

    public Optional<Metadata> getMetadataByFileHash(String fileHash) {
        return metadataRepository.findByFileHash(fileHash);
    }

    public List<Metadata> getAllMetadata() {
        return metadataRepository.findAll();
    }

    public Optional<Metadata> updateFileStatus(String fileHash, FileStatus newStatus) {
        return metadataRepository.findByFileHash(fileHash).map(metadata -> {
            metadata.setFileStatus(newStatus);
            return metadataRepository.save(metadata);
        });
    }
}

