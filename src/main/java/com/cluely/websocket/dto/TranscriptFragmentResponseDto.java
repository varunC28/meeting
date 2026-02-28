package com.cluely.websocket.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class TranscriptFragmentResponseDto {

    private String type = "TRANSCRIPT_FRAGMENT";
    private UUID meetingId;
    private UUID transcriptId;
    private String text;
    private String speaker;
    private Double startTime;
    private Double endTime;
    private Double confidence;
    private LocalDateTime timestamp;

    public TranscriptFragmentResponseDto() {
        this.timestamp = LocalDateTime.now();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UUID getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(UUID meetingId) {
        this.meetingId = meetingId;
    }

    public UUID getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(UUID transcriptId) {
        this.transcriptId = transcriptId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}