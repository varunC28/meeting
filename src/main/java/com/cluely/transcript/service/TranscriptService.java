package com.cluely.transcript.service;

import com.cluely.global.NotFoundException;
import com.cluely.transcript.dto.*;
import com.cluely.transcript.entity.TranscriptEntity;
import com.cluely.transcript.mapper.TranscriptMapper;
import com.cluely.transcript.repository.TranscriptRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TranscriptService {

    private final TranscriptRepository repo;

    public TranscriptService(TranscriptRepository repo) {
        this.repo = repo;
    }

    public TranscriptResponseDto create(TranscriptCreateRequestDto dto) {
        var saved = repo.save(TranscriptMapper.toEntity(dto));
        return TranscriptMapper.toResponse(saved);
    }

    public Page<TranscriptResponseDto> getPaged(
            UUID meetingId,
            Pageable pageable) {
        return repo
                .findByMeetingId(meetingId, pageable)
                .map(TranscriptMapper::toResponse);
    }

    public Page<TranscriptResponseDto> findAll(Pageable pageable) {
        return repo.findAll(pageable)
                .map(TranscriptMapper::toResponse);
    }

    public TranscriptResponseDto updateTranscript(UUID id, TranscriptUpdateRequestDto dto) {
        TranscriptEntity entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Transcript not found: " + id));
        if (dto.getSpeaker() != null) {
            entity.setSpeaker(dto.getSpeaker());
        }
        entity.setText(dto.getText());
        TranscriptEntity saved = repo.save(entity);
        return TranscriptMapper.toResponse(saved);
    }

    public void deleteTranscript(UUID id) {
        TranscriptEntity entity = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Transcript not found: " + id));

        entity.setDeleted(true);
        entity.setDeletedAt(LocalDateTime.now());

        repo.save(entity);
    }

}
