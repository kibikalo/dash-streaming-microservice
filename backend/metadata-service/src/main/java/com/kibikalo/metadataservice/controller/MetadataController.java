package com.kibikalo.metadataservice.controller;

import com.kibikalo.metadataservice.model.FileStatus;
import com.kibikalo.metadataservice.model.MetadataDto;
import com.kibikalo.metadataservice.model.MetadataMapperUtil;
import com.kibikalo.metadataservice.service.MetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
public class MetadataController {

    private final MetadataService metadataService;
    private final MetadataMapperUtil metadataMapperUtil;


    @PostMapping("/save")
    public ResponseEntity<Void> saveMetadata(@RequestBody Map<String, Object> metadataMap) {
        try {
            metadataService.saveMetadataFromMap(metadataMap);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error saving metadata: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // TODO Implement getMetadataByHash
    @GetMapping("/file/{fileHash}")
    public ResponseEntity<MetadataDto> getMetadataByFileHash(@PathVariable String fileHash) {
        return metadataService.getMetadataByFileHash(fileHash)
                .map(metadataMapperUtil::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<MetadataDto>> getAllMetadata() {
        List<MetadataDto> metadataList = metadataService.getAllMetadata().stream()
                .map(metadataMapperUtil::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(metadataList);
    }

    // TODO Implement updateFileStatus
    @PatchMapping("/{fileHash}/status")
    public ResponseEntity<MetadataDto> updateFileStatus(@PathVariable String fileHash, @RequestParam("fileStatus") FileStatus fileStatus) {
        return metadataService.updateFileStatus(fileHash, fileStatus)
                .map(metadataMapperUtil::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}