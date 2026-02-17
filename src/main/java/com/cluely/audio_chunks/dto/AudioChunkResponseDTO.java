package com.cluely.audio_chunks.dto;

import com.cluely.audio_chunks.entity.ChunkStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class AudioChunkResponseDTO {

    private UUID chunkId;
    private UUID meetingId;
    private Integer sequenceNumber;
    private Long sizeBytes;
    private LocalDateTime createdAt;
    private ChunkStatus status;
    private String mimeType;

    public AudioChunkResponseDTO() {
    }

    public AudioChunkResponseDTO(UUID chunkId, UUID meetingId, Integer sequenceNumber,
            Long sizeBytes, LocalDateTime createdAt,
            ChunkStatus status, String mimeType) {
        this.chunkId = chunkId;
        this.meetingId = meetingId;
        this.sequenceNumber = sequenceNumber;
        this.sizeBytes = sizeBytes;
        this.createdAt = createdAt;
        this.status = status;
        this.mimeType = mimeType;
    }

    public UUID getChunkId() {
        return chunkId;
    }

    public void setChunkId(UUID chunkId) {
        this.chunkId = chunkId;
    }

    public UUID getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(UUID meetingId) {
        this.meetingId = meetingId;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ChunkStatus getStatus() {
        return status;
    }

    public void setStatus(ChunkStatus status) {
        this.status = status;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}