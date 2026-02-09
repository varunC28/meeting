package com.cluely.transcript.mapper;

import com.cluely.transcript.dto.*;
import com.cluely.transcript.entity.TranscriptEntity;

import java.time.LocalDateTime;

public class TranscriptMapper {

    public static TranscriptEntity toEntity(TranscriptCreateRequestDto d) {
        TranscriptEntity e = new TranscriptEntity();
        e.setMeetingId(d.getMeetingId());
        e.setSpeaker(d.getSpeaker());
        e.setText(d.getText());
        e.setStartTime(d.getStartTime());
        e.setEndTime(d.getEndTime());
        e.setConfidence(d.getConfidence());
        e.setCreatedAt(LocalDateTime.now());
        return e;
    }

    public static TranscriptResponseDto toResponse(TranscriptEntity e) {
        TranscriptResponseDto d = new TranscriptResponseDto();
        d.setTranscriptId(e.getTranscriptId());
        d.setMeetingId(e.getMeetingId());
        d.setSpeaker(e.getSpeaker());
        d.setText(e.getText());
        d.setStartTime(e.getStartTime());
        d.setEndTime(e.getEndTime());
        d.setConfidence(e.getConfidence());
        d.setCreatedAt(e.getCreatedAt());
        return d;
    }
}
