package com.cluely.meeting.dto;

import com.cluely.note.dto.NoteResponseDto;
import com.cluely.transcript.dto.TranscriptResponseDto;
import com.cluely.user.dto.UserResponseDto;

import org.springframework.data.domain.Page;

public class MeetingDashboardDto {

    private MeetingResponseDto meeting;
    private UserResponseDto user;
    private Page<NoteResponseDto> notes;
    private Page<TranscriptResponseDto> transcripts;

    public MeetingDashboardDto(
            MeetingResponseDto meeting,
            UserResponseDto user,
            Page<NoteResponseDto> notes,
            Page<TranscriptResponseDto> transcripts) {

        this.meeting = meeting;
        this.user = user;
        this.notes = notes;
        this.transcripts = transcripts;
    }

    public MeetingResponseDto getMeeting() {
        return meeting;
    }

    public UserResponseDto getUser() {
        return user;
    }

    public Page<NoteResponseDto> getNotes() {
        return notes;
    }

    public Page<TranscriptResponseDto> getTranscripts() {
        return transcripts;
    }
}
