package com.kibikalo.metadataservice.model;

import com.kibikalo.shared.model.AudioStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

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

    @Column(nullable = false, length = 1024)
    private String rawFilePath;

    @Column(length = 1024)
    private String manifestPath;

    @Column(length = 1024)
    private String segmentBasePath;

    private String rawFileFormat;

    private Long rawFileSize;

    private Long durationMillis;

    private String codec;

    @ElementCollection(fetch = FetchType.EAGER) // Store list as separate table or array
    @CollectionTable(name = "audio_bitrates", joinColumns = @JoinColumn(name = "audio_id"))
    private List<Integer> bitratesKbps;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private Instant encodingTimestamp;

    public AudioMetadata(
            String id,
            String originalFileName,
            String rawFilePath
    ) {
        this.id = id;
        this.originalFileName = originalFileName;
        this.rawFilePath = rawFilePath;
        this.status = AudioStatus.PENDING_ENCODING;
    }
}