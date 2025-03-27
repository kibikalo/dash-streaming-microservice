package com.kibikalo.metadataservice.service;

import com.kibikalo.metadataservice.model.FileStatus;
import com.kibikalo.metadataservice.model.Metadata;
import com.kibikalo.metadataservice.model.MetadataDto;
import com.kibikalo.metadataservice.model.MetadataMapperUtil;
import com.kibikalo.metadataservice.repo.MetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MetadataService {

    private final MetadataRepository metadataRepository;
    private final MetadataMapperUtil metadataMapperUtil;

    public Metadata saveMetadata(Metadata metadata) {
        return metadataRepository.save(metadata);
    }

    public void saveMetadataFromMap(Map<String, Object> metadataMap) {
        String fileHash = (String) metadataMap.get("fileHash");

        if (metadataRepository.existsByFileHash(fileHash)) {
            System.out.println("Metadata with fileHash " + fileHash + " already exists. Skipping insertion.");
            throw new HttpClientErrorException(HttpStatus.CONFLICT, "Metadata with this file hash already exists.");
        }

        MetadataDto metadataDTO = new MetadataDto();
        metadataDTO.setFileHash(fileHash);
        metadataDTO.setFilePath((String) metadataMap.get("filePath"));
        metadataDTO.setFileFormat((String) metadataMap.get("fileFormat"));

        // FIXME There was error with casting Integer to Long, temporary fix, refactor later and find cause
//        metadataDTO.setFileSize((Long) metadataMap.get("fileSize"));
        Object fileSizeObject = metadataMap.get("fileSize");
        if(fileSizeObject instanceof Integer){
            Integer fileSizeInt = (Integer) fileSizeObject;
            metadataDTO.setFileSize(fileSizeInt.longValue()); // Cast Integer to Long
        }else if (fileSizeObject instanceof Long){
            metadataDTO.setFileSize((Long) metadataMap.get("fileSize"));
        } else {
            System.err.println("Unexpected class: "+ fileSizeObject.getClass());
        }


        metadataDTO.setDuration((int) metadataMap.get("duration"));
        metadataDTO.setCodec((String) metadataMap.get("codec"));

        Metadata metadata = metadataMapperUtil.toEntity(metadataDTO);

        // FIXME Remove debug souts later
        System.out.println("Received Metadata: ");
        System.out.println(metadataMap.get("fileHash"));
        System.out.println(metadataMap.get("filePath"));
        System.out.println(metadataMap.get("fileFormat"));
        System.out.println(metadataMap.get("fileSize"));
        System.out.println(metadataMap.get("duration"));
        System.out.println(metadataMap.get("codec"));

        try {
            metadataRepository.save(metadata);
        } catch (DataIntegrityViolationException e) {
            System.err.println("Failed to save metadata due to unique constraint violation: " + e.getMessage());
        }
    }

    public Optional<Metadata> getMetadataById(Long id) {
        return metadataRepository.findById(id);
    }

    public Optional<Metadata> getMetadataByFileHash(String fileHash) {
        return metadataRepository.findByFileHash(fileHash);
    }

    public List<Metadata> getAllMetadata() {
        return metadataRepository.findAll();
    }

    public Optional<Metadata> updateFileStatus(String fileHash, FileStatus newStatus) {
        return metadataRepository.findByFileHash(fileHash).map(metadata -> {
            metadata.setFileStatus(newStatus);
            return metadataRepository.save(metadata);
        });
    }


}

