package com.cluely.note.mapper;

import com.cluely.note.dto.*;
import com.cluely.note.entity.NoteEntity;

import java.time.LocalDateTime;

public class NoteMapper {

    public static NoteEntity toEntity(NoteCreateRequestDto d) {
        NoteEntity e = new NoteEntity();
        e.setUserId(d.getUserId());
        e.setMeetingId(d.getMeetingId());
        e.setContent(d.getContent());
        e.setCreatedAt(LocalDateTime.now());
        return e;
    }

    public static NoteResponseDto toResponse(NoteEntity e) {
        NoteResponseDto d = new NoteResponseDto();
        d.setNoteId(e.getNoteId());
        d.setUserId(e.getUserId());
        d.setMeetingId(e.getMeetingId());
        d.setContent(e.getContent());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }
}
