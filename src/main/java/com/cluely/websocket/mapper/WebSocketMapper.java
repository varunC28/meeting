package com.cluely.websocket.mapper;

import com.cluely.transcript.entity.TranscriptEntity;
import com.cluely.websocket.dto.TranscriptFragmentResponseDto;
import org.springframework.stereotype.Component;

@Component
public class WebSocketMapper {

    public TranscriptFragmentResponseDto toResponse(TranscriptEntity entity) {
        TranscriptFragmentResponseDto dto = new TranscriptFragmentResponseDto();
        dto.setMeetingId(entity.getMeetingId());
        dto.setTranscriptId(entity.getTranscriptId());
        dto.setText(entity.getText());
        dto.setSpeaker(entity.getSpeaker());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setConfidence(entity.getConfidence());
        return dto;
    }
}