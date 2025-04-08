package com.kibikalo.streamingservice.controller;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kibikalo.streamingservice.exception.ResourceNotFoundException;
import com.kibikalo.streamingservice.exception.ResourceNotReadyException;
import com.kibikalo.streamingservice.service.StreamingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/stream")
@RequiredArgsConstructor
@Slf4j
public class StreamingController {

    private final StreamingService streamingService;

    @GetMapping("/{audioId}/manifest.mpd")
    public ResponseEntity<Void> getManifestRedirect(@PathVariable("audioId") String audioId) {
        try {
            String manifestUrl = streamingService.getManifestUrl(audioId);

            // Create HTTP 302 Redirect response
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(manifestUrl));
            return new ResponseEntity<>(headers, HttpStatus.FOUND); // 302 Found

        } catch (ResourceNotFoundException e) {
            log.warn("Manifest request failed - Not Found: {}", e.getMessage());
            // Let Spring handle via @ResponseStatus on exception
            throw e;
        } catch (ResourceNotReadyException e) {
            log.warn("Manifest request failed - Not Ready: {}", e.getMessage());
            // Let Spring handle via @ResponseStatus on exception
            throw e;
        } catch (Exception e) {
            log.error("Internal server error processing manifest request for audioId {}: {}", audioId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Optional: Centralized exception handling within controller
    // Or use @ControllerAdvice for global handling

     @ExceptionHandler(ResourceNotFoundException.class)
     public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
     }

     @ExceptionHandler(ResourceNotReadyException.class)
     public ResponseEntity<String> handleNotReady(ResourceNotReadyException ex) {
         return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
     }

     @ExceptionHandler(Exception.class)
     public ResponseEntity<String> handleGenericError(Exception ex) {
         log.error("Unhandled exception in StreamingController: {}", ex.getMessage(), ex);
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
     }
}