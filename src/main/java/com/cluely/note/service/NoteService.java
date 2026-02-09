package com.cluely.note.service;

import com.cluely.global.NotFoundException;
import com.cluely.note.dto.*;
import com.cluely.note.entity.NoteEntity;
import com.cluely.note.mapper.NoteMapper;
import com.cluely.note.repository.NoteRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NoteService {

    private final NoteRepository repo;

    public NoteService(NoteRepository repo) {
        this.repo = repo;
    }

    public NoteResponseDto create(NoteCreateRequestDto dto) {
        var saved = repo.save(NoteMapper.toEntity(dto));
        return NoteMapper.toResponse(saved);
    }

    public Page<NoteResponseDto> getNotes(
            UUID userId,
            UUID meetingId,
            Pageable pageable) {

        Page<NoteEntity> page;

        if (meetingId != null) {
            page = repo.findByMeetingId(meetingId, pageable);
        } else if (userId != null) {
            page = repo.findByUserId(userId, pageable);
        } else {
            page = repo.findAll(pageable);
        }

        return page.map(NoteMapper::toResponse);
    }

    public NoteResponseDto update(UUID id, NoteCreateRequestDto dto) {
        NoteEntity e = repo.findById(id).orElseThrow();
        e.setContent(dto.getContent());
        e.setUpdatedAt(LocalDateTime.now());
        return NoteMapper.toResponse(repo.save(e));
    }

    // Update
    public NoteResponseDto updateNote(UUID id, NoteUpdateRequestDto dto) {
        NoteEntity note = repo.findById(id).orElseThrow(() -> new NotFoundException("Note notfound: " + id));
        note.setContent(dto.getContent());
        note.setUpdatedAt(LocalDateTime.now());
        NoteEntity saved = repo.save(note);
        return NoteMapper.toResponse(saved);
    }

    // Delete
    public void deleteNote(UUID id) {
        NoteEntity note = repo.findById(id).orElseThrow(() -> new NotFoundException("Note not found: " + id));

        note.setDeleted(true);
        note.setDeletedAt(LocalDateTime.now());

        repo.save(note);
    }

    // Soft Delete
    public Page<NoteResponseDto> getActiveNotesByMeetingId(UUID meetingId, Pageable pageable) {
        return repo.findByMeetingIdAndDeletedFalse(meetingId, pageable)
                .map(NoteMapper::toResponse);
    }
}
