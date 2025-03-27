package com.kibikalo.uploadservice.controller;

import com.kibikalo.uploadservice.dto.AudioMetadataDto;
import com.kibikalo.uploadservice.service.UploadService;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/upload")
@AllArgsConstructor
public class UploadController {

    private final UploadService uploadService;
    private final RestTemplate restTemplate;

    @PostMapping()
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            if (!uploadService.isValidAudioFile(file)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid file type. Only audio formats (mp3, wav, etc.) are allowed.");
            }

            AudioMetadataDto metadata = uploadService.processFile(file);

            // Log metadata
            System.out.println("File Uploaded: " + metadata);

            // Return metadata as response
            // FIXME It always responds with status OK, even if metadata service returns error status 409 (Trying to save existing value file).
            return ResponseEntity.status(HttpStatus.OK).body(metadata);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload file: " + e.getMessage());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Metadata for this file already exists.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to save metadata: " + e.getMessage());
            }
        }
    }
}
