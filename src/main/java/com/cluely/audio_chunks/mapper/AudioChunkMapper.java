package com.cluely.audio_chunks.mapper;

import com.cluely.audio_chunks.dto.AudioChunkResponseDTO;
import com.cluely.audio_chunks.dto.ChunkProgressResponseDTO;
import com.cluely.audio_chunks.entity.AudioChunkEntity;
import com.cluely.meeting.entity.MeetingStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AudioChunkMapper {

    public AudioChunkResponseDTO toResponseDTO(AudioChunkEntity entity) {
        return new AudioChunkResponseDTO(
                entity.getChunkId(),
                entity.getMeeting().getMeetingId(),
                entity.getSequenceNumber(),
                entity.getSizeBytes(),
                entity.getCreatedAt(),
                entity.getStatus(),
                entity.getMimeType());
    }

    public List<AudioChunkResponseDTO> toResponseDTOList(List<AudioChunkEntity> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ChunkProgressResponseDTO toProgressDTO(long totalChunks, int maxSequence, MeetingStatus status) {
        return new ChunkProgressResponseDTO(totalChunks, maxSequence, status);
    }
}