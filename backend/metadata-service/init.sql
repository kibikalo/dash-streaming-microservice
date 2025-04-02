-- Drop table if it exists (for easy restarts during dev)
DROP TABLE IF EXISTS audio_metadata;

-- Create the audio_metadata table
CREATE TABLE audio_metadata (
                                id VARCHAR(255) PRIMARY KEY,
                                original_file_name VARCHAR(255) NOT NULL,
                                raw_file_path VARCHAR(1024) NOT NULL,
                                status VARCHAR(50) NOT NULL,
                                manifest_path VARCHAR(1024),
                                duration_millis BIGINT,
                                created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
    -- Add other columns as needed (bitrates, codecs, user_id, etc.)
    -- Add indexes for columns frequently queried, e.g., status
    -- CREATE INDEX idx_audio_metadata_status ON audio_metadata(status);
);