package com.cluely.ai.analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class AnalysisRequestDTO {

    @NotNull
    private UUID meetingId;

    @NotNull
    private UUID userId;

    @NotBlank
    private String fullTranscript;

    private String meetingTitle;

    public AnalysisRequestDTO() {
    }

    public AnalysisRequestDTO(UUID meetingId, UUID userId,
            String fullTranscript, String meetingTitle) {
        this.meetingId = meetingId;
        this.userId = userId;
        this.fullTranscript = fullTranscript;
        this.meetingTitle = meetingTitle;
    }

    public UUID getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(UUID meetingId) {
        this.meetingId = meetingId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFullTranscript() {
        return fullTranscript;
    }

    public void setFullTranscript(String fullTranscript) {
        this.fullTranscript = fullTranscript;
    }

    public String getMeetingTitle() {
        return meetingTitle;
    }

    public void setMeetingTitle(String meetingTitle) {
        this.meetingTitle = meetingTitle;
    }
}