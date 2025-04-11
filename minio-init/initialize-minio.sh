#!/bin/sh
# Script to initialize MinIO buckets and policies after server starts

# Wait a few seconds for MinIO server to be likely ready
sleep 5

echo "Attempting to configure MinIO..."

# Configure mc alias for localhost inside the container
# Use --config-dir to avoid permission issues with /root/.mc
mc --config-dir /tmp/.mc alias set local http://localhost:9000 minioadmin minioadmin

# Check if buckets exist, create if not
mc --config-dir /tmp/.mc ls local/raw-audio > /dev/null 2>&1
if [ $? -ne 0 ]; then
  echo "Creating bucket: raw-audio"
  mc --config-dir /tmp/.mc mb local/raw-audio
else
  echo "Bucket already exists: raw-audio"
fi

mc --config-dir /tmp/.mc ls local/processed-audio > /dev/null 2>&1
if [ $? -ne 0 ]; then
  echo "Creating bucket: processed-audio"
  mc --config-dir /tmp/.mc mb local/processed-audio
  # Apply the custom policy only when creating the processed bucket
  echo "Applying custom policy from /init/processed-audio-policy.json to bucket: processed-audio"
    mc --config-dir /tmp/.mc anonymous set-json /init/processed-audio-policy.json local/processed-audio
    if [ $? -ne 0 ]; then
        echo "ERROR: Failed to set custom policy for processed-audio bucket!"
        # Optionally exit with error: exit 1
    fi
else
  echo "Bucket already exists: processed-audio"
  echo "Ensuring custom policy is set for bucket: processed-audio"
  mc --config-dir /tmp/.mc anonymous set-json /init/processed-audio-policy.json local/processed-audio
   if [ $? -ne 0 ]; then
      echo "ERROR: Failed to ensure custom policy for processed-audio bucket!"
  fi
fi

echo "Verifying applied anonymous policy for processed-audio:"
mc --config-dir /tmp/.mc anonymous get local/processed-audio

echo "MinIO initialization script finished."

exit 0