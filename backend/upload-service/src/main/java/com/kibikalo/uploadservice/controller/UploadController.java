package com.kibikalo.uploadservice.controller;

import com.kibikalo.uploadservice.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
@Slf4j
public class UploadController {

    private final UploadService uploadService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        log.info(
                "Received file upload request: {}",
                file.getOriginalFilename()
        );
        try {
            String audioId = uploadService.uploadAudio(file);
            // Return the generated audioId in the response body
            return ResponseEntity.ok(audioId);
        } catch (IllegalArgumentException e) {
            log.warn("Upload failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Internal server error during upload", e);
            return ResponseEntity.internalServerError()
                    .body("Upload failed due to an internal error.");
        }
    }
}