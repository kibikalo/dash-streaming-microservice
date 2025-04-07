-- Drop table if it exists (for easy restarts during dev)
DROP TABLE IF EXISTS audio_bitrates; -- Drop dependent table first if using @ElementCollection default
DROP TABLE IF EXISTS audio_metadata;

-- Create the audio_metadata table
CREATE TABLE audio_metadata (
                                id VARCHAR(255) PRIMARY KEY,
                                original_file_name VARCHAR(255) NOT NULL,
                                raw_file_path VARCHAR(1024) NOT NULL,
                                status VARCHAR(50) NOT NULL,
                                manifest_path VARCHAR(1024),
                                duration_millis BIGINT,
    -- New Columns --
                                segment_base_path VARCHAR(1024),
                                codec VARCHAR(50),
                                encoding_timestamp TIMESTAMP WITH TIME ZONE,
                                raw_file_size BIGINT,
                                raw_file_format VARCHAR(100),
    -- ------------- --
                                created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create table for bitrates if using @ElementCollection default strategy
CREATE TABLE audio_bitrates (
                                audio_id VARCHAR(255) NOT NULL REFERENCES audio_metadata(id) ON DELETE CASCADE,
                                bitrate_kbps INTEGER
);

-- Optional: Function to update updated_at timestamp automatically
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = NOW();
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_audio_metadata_updated_at
    BEFORE UPDATE ON audio_metadata
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Optional: Indexes
CREATE INDEX idx_audio_metadata_status ON audio_metadata(status);
