package com.cluely.ai.speech.mapper;

import com.cluely.ai.speech.dto.TranscriptionResponseDTO;
import com.cluely.transcript.entity.TranscriptEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TranscriptionMapper {

    public TranscriptEntity toEntity(TranscriptionResponseDTO response) {
        TranscriptEntity entity = new TranscriptEntity();
        entity.setMeetingId(response.getMeetingId());
        entity.setText(response.getText());
        entity.setSpeaker(response.getSpeaker() != null ? response.getSpeaker() : "Unknown");
        entity.setStartTime(response.getStartTime());
        entity.setEndTime(response.getEndTime());
        entity.setConfidence(response.getConfidence());
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    public TranscriptionResponseDTO toResponse(TranscriptEntity entity) {
        TranscriptionResponseDTO response = new TranscriptionResponseDTO();
        response.setMeetingId(entity.getMeetingId());
        response.setText(entity.getText());
        response.setSpeaker(entity.getSpeaker());
        response.setStartTime(entity.getStartTime());
        response.setEndTime(entity.getEndTime());
        response.setConfidence(entity.getConfidence());
        return response;
    }
}