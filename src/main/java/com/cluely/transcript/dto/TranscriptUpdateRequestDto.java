package com.cluely.transcript.dto;

import jakarta.validation.constraints.NotBlank;

public class TranscriptUpdateRequestDto {

    @NotBlank
    private String speaker;

    @NotBlank
    private String text;

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

}
