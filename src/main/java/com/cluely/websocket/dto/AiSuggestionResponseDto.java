package com.cluely.websocket.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class AiSuggestionResponseDto {

    private String type = "AI_SUGGESTION";
    private UUID meetingId;
    private String category;
    private String priority;
    private String title;
    private String content;
    private UUID sourceNoteId;
    private LocalDateTime timestamp;

    public AiSuggestionResponseDto() {
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UUID getSourceNoteId() {
        return sourceNoteId;
    }

    public void setSourceNoteId(UUID sourceNoteId) {
        this.sourceNoteId = sourceNoteId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}