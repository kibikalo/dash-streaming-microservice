package com.kibikalo.metadataservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kibikalo.metadataservice.service.MetadataService;
import com.kibikalo.shared.dto.AudioMetadataDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/metadata")
@RequiredArgsConstructor
@Slf4j
public class MetadataController {

    private final MetadataService metadataService;

    @GetMapping("/status/{audioId}")
    public ResponseEntity<AudioMetadataDto> getMetadata(@PathVariable("audioId") String audioId) {
        log.info("Received request for metadata for audioId: {}", audioId);

        AudioMetadataDto metadataDTO = metadataService.getAudioMetadataById(audioId);

        return ResponseEntity.ok(metadataDTO);
    }

    // Optional: Add @ControllerAdvice for centralized exception handling
    // MetadataNotFoundException will trigger the 404 response.
}