package com.kibikalo.shared.model;

public enum AudioStatus {
    PENDING_ENCODING, // Initial state after upload received
    ENCODING_IN_PROGRESS, // When encoding service picks it up (set later)
    AVAILABLE, // Encoding complete, ready for streaming (set later)
    FAILED_ENCODING, // Encoding failed (set later)
    DELETED
}
