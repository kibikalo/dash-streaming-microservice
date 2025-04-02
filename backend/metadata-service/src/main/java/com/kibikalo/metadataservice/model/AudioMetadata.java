package com.kibikalo.metadataservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "audio_metadata")
@Getter
@Setter
@NoArgsConstructor
public class AudioMetadata {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String originalFileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AudioStatus status;

    private String rawFilePath;

    private String fileFormat;

    private Long fileSize;      // Size in bytes

    private Long durationMillis;

    private String codec;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public AudioMetadata(
            String id,
            String originalFileName,
            String rawFilePath,
            AudioStatus status
    ) {
        this.id = id;
        this.originalFileName = originalFileName;
        this.rawFilePath = rawFilePath;
        this.status = status;
    }
}