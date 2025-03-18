package com.kibikalo.metadataservice.repo;

import com.kibikalo.metadataservice.model.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MetadataRepository extends JpaRepository<Metadata, Long> {
    Optional<Metadata> findByFileHash(String fileHash);
}

