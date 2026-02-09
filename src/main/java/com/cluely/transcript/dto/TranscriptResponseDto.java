package com.cluely.transcript.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class TranscriptResponseDto {

    private UUID transcriptId;
    private UUID meetingId;
    private String speaker;
    private String text;
    private Double startTime;
    private Double endTime;
    private Double confidence;
    private LocalDateTime createdAt;

    public UUID getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(UUID transcriptId) {
        this.transcriptId = transcriptId;
    }

    public UUID getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(UUID meetingId) {
        this.meetingId = meetingId;
    }

    public String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Double getStartTime() {
        return startTime;
    }

    public void setStartTime(Double startTime) {
        this.startTime = startTime;
    }

    public Double getEndTime() {
        return endTime;
    }

    public void setEndTime(Double endTime) {
        this.endTime = endTime;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
