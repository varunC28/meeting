package com.cluely.websocket.dto;

import java.util.UUID;

public class ChunkUploadedRequestDto {

    private UUID chunkId;
    private Integer sequenceNumber;

    public ChunkUploadedRequestDto() {
    }

    public UUID getChunkId() {
        return chunkId;
    }

    public void setChunkId(UUID chunkId) {
        this.chunkId = chunkId;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}