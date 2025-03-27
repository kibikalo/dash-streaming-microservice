package com.kibikalo.uploadservice.service;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class MetadataServiceClient {

    private final LoadBalancerClient loadBalancerClient;
    private final RestTemplate restTemplate;

    public MetadataServiceClient(LoadBalancerClient loadBalancerClient, RestTemplate restTemplate) {
        this.loadBalancerClient = loadBalancerClient;
        this.restTemplate = restTemplate;
    }

    public void saveMetadata(String fileHash, String filePath, String fileFormat, Long fileSize, int duration, String codec) {
        // Create the metadata Map
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fileHash", fileHash);
        metadata.put("filePath", filePath);
        metadata.put("fileFormat", fileFormat);
        metadata.put("fileSize", fileSize);
        metadata.put("duration", duration);
        metadata.put("codec", codec);

        // Get Service Instance using LoadBalancerClient
        ServiceInstance serviceInstance = loadBalancerClient.choose("metadata-service");

        if (serviceInstance != null) {
            String baseUrl = serviceInstance.getUri().toString();
            String metadataServiceUrl = baseUrl + "/api/metadata/save";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(metadata, headers);

            try {
                ResponseEntity<Void> response = restTemplate.postForEntity(metadataServiceUrl, request, Void.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Metadata saved successfully");
                } else {
                    System.err.println("Failed to save metadata. Status code: " + response.getStatusCode());
                }
            } catch (Exception e) {
                System.err.println("Error during metadata save: " + e.getMessage());
            }

        } else {
            System.err.println("Metadata service not available");
        }
    }
}
