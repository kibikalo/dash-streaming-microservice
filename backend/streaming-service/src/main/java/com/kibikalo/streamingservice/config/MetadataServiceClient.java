package com.kibikalo.streamingservice.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.kibikalo.shared.dto.AudioMetadataDto;

@FeignClient(name = "${app.metadata-service-name}")
public interface MetadataServiceClient {

    // Path matches the endpoint we expect on Metadata Service
    @GetMapping("/api/v1/metadata/status/{audioId}")
    AudioMetadataDto getMetadata(@PathVariable("audioId") String audioId);

}
