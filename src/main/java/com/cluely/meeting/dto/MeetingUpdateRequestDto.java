package com.cluely.meeting.dto;

import jakarta.validation.constraints.NotBlank;

public class MeetingUpdateRequestDto {

    @NotBlank
    private String title;

    @NotBlank
    private String source;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
