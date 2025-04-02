package com.kibikalo.metadataservice;

import com.kibikalo.metadataservice.event.incoming.AudioUploadedEvent;
import com.kibikalo.metadataservice.model.AudioMetadata;
import com.kibikalo.metadataservice.model.AudioStatus;
import com.kibikalo.metadataservice.event.outgoing.EncodingRequestedEvent;
import com.kibikalo.metadataservice.repo.AudioMetadataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AudioEventListener {

    private final AudioMetadataRepository metadataRepository;
    private final KafkaTemplate<String, EncodingRequestedEvent> kafkaTemplate;

    @Value("${app.kafka.topic.encoding-requested}")
    private String encodingRequestedTopic;

    // Define the topic name directly or load from properties
    private static final String UPLOAD_TOPIC = "audio.uploaded";

    @KafkaListener(
            topics = UPLOAD_TOPIC,
            groupId = "${spring.kafka.consumer.group-id}", // Use group ID from properties
            // Specify container factory if you have custom configurations
            // containerFactory = "kafkaListenerContainerFactory"
            // Specify the type for the JsonDeserializer
            properties = {
                    "spring.json.value.default.type=com.kibikalo.metadataservice.event.incoming.AudioUploadedEvent" }
    )
    @Transactional // Make DB save and Kafka publish atomic (within this service)
    public void handleAudioUploadedEvent(@Payload AudioUploadedEvent event) {
        log.info(
                "Received AudioUploadedEvent for audioId: {}",
                event.getAudioId()
        );

        // Basic validation
        if (event.getAudioId() == null || event.getRawFilePath() == null) {
            log.error("Received invalid AudioUploadedEvent: {}", event);
            // Consider sending to a Dead Letter Queue (DLQ)
            return;
        }

        // Check if metadata already exists (handle potential duplicate messages)
        if (metadataRepository.existsById(event.getAudioId())) {
            log.warn(
                    "Metadata for audioId: {} already exists. Ignoring duplicate event.",
                    event.getAudioId()
            );
            return; // Or potentially re-trigger encoding if status indicates failure?
        }

        try {
            // Create and save initial metadata
            AudioMetadata metadata = new AudioMetadata(
                    event.getAudioId(),
                    event.getOriginalFileName(),
                    event.getRawFilePath(),
                    AudioStatus.PENDING_ENCODING // Initial status
            );
            metadataRepository.save(metadata);
            log.info(
                    "Saved initial metadata for audioId: {}",
                    event.getAudioId()
            );

            // Create the event to trigger encoding
            EncodingRequestedEvent encodingEvent = new EncodingRequestedEvent(
                    event.getAudioId(),
                    event.getRawFilePath()
            );

            // Publish EncodingRequestedEvent
            kafkaTemplate.send(
                    encodingRequestedTopic,
                    event.getAudioId(), // Use audioId as key
                    encodingEvent
            );
            log.info(
                    "Published EncodingRequestedEvent for audioId: {} to topic: {}",
                    event.getAudioId(),
                    encodingRequestedTopic
            );

        } catch (Exception e) {
            log.error(
                    "Error processing AudioUploadedEvent for audioId {}: {}",
                    event.getAudioId(),
                    e.getMessage(),
                    e
            );
            // Transactional should rollback DB changes.
            // Consider retry mechanisms or DLQ for Kafka publish failures.
            // Re-throwing might cause Kafka to redeliver (depending on config)
            throw new RuntimeException(
                    "Failed to process AudioUploadedEvent",
                    e
            );
        }
    }
}
