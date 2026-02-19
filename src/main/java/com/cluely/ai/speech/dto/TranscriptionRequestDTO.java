package com.cluely.ai.speech.dto;

import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import java.util.UUID;

public class TranscriptionRequestDTO {

    @NotNull
    private UUID meetingId;

    @NotNull
    private Path audioFilePath;

    private Integer sequenceNumber; // null for full audio

    private String language = "en"; // default English

    private boolean detectSpeakers = false; // speaker diarization

    public TranscriptionRequestDTO() {
    }

    public TranscriptionRequestDTO(UUID meetingId, Path audioFilePath, Integer sequenceNumber) {
        this.meetingId = meetingId;
        this.audioFilePath = audioFilePath;
        this.sequenceNumber = sequenceNumber;
    }

    public UUID getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(UUID meetingId) {
        this.meetingId = meetingId;
    }

    public Path getAudioFilePath() {
        return audioFilePath;
    }

    public void setAudioFilePath(Path audioFilePath) {
        this.audioFilePath = audioFilePath;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isDetectSpeakers() {
        return detectSpeakers;
    }

    public void setDetectSpeakers(boolean detectSpeakers) {
        this.detectSpeakers = detectSpeakers;
    }
}