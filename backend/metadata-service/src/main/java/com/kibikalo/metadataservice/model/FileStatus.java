package com.kibikalo.metadataservice.model;

public enum FileStatus {
    METADATA_CREATED,   // Metadata has been created but not fully processed or validated yet
    UPLOADED,           // File has been uploaded and stored
    ENCODING,           // File is being processed
    PROCESSED,          // File has been successfully processed
    FAILED,             // An error occurred during encoding or processing
    READY               // The entire lifecycle is complete and the file is available for streaming
}
