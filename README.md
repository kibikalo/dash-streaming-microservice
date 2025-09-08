# DASH Audio Streaming Microservices

A microservice-based audio streaming system using DASH, MinIO, Kafka, and Spring Boot.

This project demonstrates a fully **event-driven architecture** for processing, encoding, storing, and streaming audio files. It is designed to be **modular and scalable,** with separate services handling upload, metadata management, encoding, and streaming.

## Features

**Upload Service:**
- Upload audio files and validate metadata using JAudioTagger
- Extract and store basic metadata for later processing

**Metadata Service:**
- Manage audio status and metadata in PostgreSQL
- Expose endpoints to query audio availability and manifest paths

**Encoding Service:**
- DASH encode uploaded audio into segments and generate MPD manifests
- Store processed audio in MinIO
- Publish encoding success/failure events to Kafka

**Streaming Service:**
- Serve audio manifests (.mpd) for DASH streaming
- Generate presigned URLs for secure access to MinIO-stored content
- Integrates with Metadata Service to ensure only available audio is served

**Infrastructure & DevOps Features:**
- MinIO buckets auto-created on startup with proper policies
- Nginx reverse proxy for external access and presigned URL validation
- Event-driven communication between services via Kafka
- Docker Compose setup for local development and testing

## Architecture Overview

```
[ Upload Service ] --> Kafka --> [ Encoding Service ] --> MinIO
        |                                         |
        v                                         v
[ Metadata Service ] <-------------------- [ Streaming Service ]
```

- Services communicate via Kafka events (EncodingRequestedEvent, EncodingSucceededEvent, EncodingFailedEvent)

- MinIO stores both raw uploads and DASH-encoded audio segments

- Streaming Service generates presigned URLs to deliver content to clients

## Tech Stack
- Java / Spring Boot
- Apache Kafka for event-driven communication
- MinIO for object storage
- PostgreSQL for metadata persistence
- FFmpeg for DASH encoding
- Docker & Docker Compose for local development
- Nginx as a reverse proxy for MinIO

## Getting Started

Clone the repository:


```
git clone https://github.com/kibikalo/dash-streaming-microservice
cd dash-audio-microservices
```

Start services with Docker Compose:

```
docker-compose up --build
```
Upload an audio file via the Upload Service API, which triggers encoding and metadata updates.

Access the DASH manifest via the Streaming Service API:

```
GET /stream/{audioId}/manifest.mpd
```
