package com.kibikalo.encodingservice;

import com.kibikalo.encodingservice.service.EncodingService;
import com.kibikalo.shared.events.EncodingRequestedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventListener {

    private final EncodingService encodingService;

    // Consume from the topic defined in application.yml
    @KafkaListener(
            topics = "${app.kafka.topic.encoding-requested}",
            groupId = "${spring.kafka.consumer.group-id}",
            // Ensure the deserializer knows the target type
            properties = {
                    "spring.json.value.default.type=com.kibikalo.shared.events.EncodingRequestedEvent" }
    )
    public void handleEncodingRequestedEvent(
            @Payload EncodingRequestedEvent event
    ) {
        log.info(
                "Received EncodingRequestedEvent for audioId: {}",
                event.getAudioId()
        );
        try {
            // Delegate processing to the service layer
            encodingService.processEncodingRequest(event);
        } catch (Exception e) {
            // Service layer should handle publishing failure event,
            // but log unexpected errors here.
            log.error(
                    "Unexpected error during top-level handling of EncodingRequestedEvent for audioId {}: {}",
                    event.getAudioId(),
                    e.getMessage(),
                    e
            );
            // Depending on error handling strategy, might need to manually publish
            // failure event here if the service couldn't.
        }
    }
}