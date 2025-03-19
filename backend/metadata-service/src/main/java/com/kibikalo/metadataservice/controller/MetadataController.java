package com.kibikalo.metadataservice.controller;

import com.kibikalo.metadataservice.model.FileStatus;
import com.kibikalo.metadataservice.model.Metadata;
import com.kibikalo.metadataservice.model.MetadataDto;
import com.kibikalo.metadataservice.model.MetadataMapperUtil;
import com.kibikalo.metadataservice.service.MetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
public class MetadataController {

    private final MetadataService metadataService;
    private final MetadataMapperUtil metadataMapperUtil;


    @PostMapping
    public ResponseEntity<MetadataDto> saveMetadata(@RequestBody MetadataDto metadataDTO) {
        Metadata metadata = metadataMapperUtil.toEntity(metadataDTO);
        Metadata savedMetadata = metadataService.saveMetadata(metadata);
        return ResponseEntity.ok(metadataMapperUtil.toDto(savedMetadata));
    }

    // doesn't work yet
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

    // doesn't work yet
    @PatchMapping("/{fileHash}/status")
    public ResponseEntity<MetadataDto> updateFileStatus(@PathVariable String fileHash, @RequestParam("fileStatus") FileStatus fileStatus) {
        return metadataService.updateFileStatus(fileHash, fileStatus)
                .map(metadataMapperUtil::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}

