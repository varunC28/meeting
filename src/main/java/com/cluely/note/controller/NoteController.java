package com.cluely.note.controller;

import com.cluely.note.dto.*;
import com.cluely.note.service.NoteService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/notes")
public class NoteController {

    private final NoteService service;

    public NoteController(NoteService service) {
        this.service = service;
    }

    @PostMapping
    public NoteResponseDto create(
            @Valid @RequestBody NoteCreateRequestDto dto) {
        return service.create(dto);
    }

    @GetMapping
    public Page<NoteResponseDto> list(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID meetingId,
            Pageable pageable) {

        return service.getNotes(userId, meetingId, pageable);
    }

    @PutMapping("/{id}")
    public NoteResponseDto update(
            @PathVariable UUID id,
            @Valid @RequestBody NoteUpdateRequestDto dto) {
        return service.updateNote(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.deleteNote(id);
    }
}
