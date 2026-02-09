package com.cluely.transcript.controller;

import com.cluely.transcript.dto.*;
import com.cluely.transcript.service.TranscriptService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transcripts")
public class TranscriptController {

    private final TranscriptService service;

    public TranscriptController(TranscriptService service) {
        this.service = service;
    }

    @PostMapping
    public TranscriptResponseDto create(
            @Valid @RequestBody TranscriptCreateRequestDto dto) {
        return service.create(dto);
    }

    @GetMapping
    public Page<TranscriptResponseDto> list(
            @RequestParam UUID meetingId,
            Pageable pageable) {
        return service.getPaged(meetingId, pageable);
    }

    @PutMapping("/{id}")
    public TranscriptResponseDto update(
            @PathVariable UUID id,
            @Valid @RequestBody TranscriptUpdateRequestDto dto) {

        return service.updateTranscript(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.deleteTranscript(id);
    }

}
