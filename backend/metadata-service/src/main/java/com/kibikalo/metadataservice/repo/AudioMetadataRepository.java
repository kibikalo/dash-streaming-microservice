package com.kibikalo.metadataservice.repo;

import com.kibikalo.metadataservice.model.AudioMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AudioMetadataRepository extends JpaRepository<AudioMetadata, String> {
}