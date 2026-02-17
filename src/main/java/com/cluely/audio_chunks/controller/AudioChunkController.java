package com.cluely.audio_chunks.controller;

import com.cluely.audio_chunks.dto.AudioChunkResponseDTO;
import com.cluely.audio_chunks.dto.ChunkProgressResponseDTO;
import com.cluely.audio_chunks.service.AudioChunkService;
import com.cluely.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meetings/{meetingId}/chunks")
public class AudioChunkController {

    private final AudioChunkService chunkService;

    @Autowired
    public AudioChunkController(AudioChunkService chunkService) {
        this.chunkService = chunkService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AudioChunkResponseDTO> uploadChunk(
            @PathVariable UUID meetingId,
            @RequestParam("sequenceNumber") Integer sequenceNumber,
            @RequestParam("audioFile") MultipartFile audioFile) {

        UUID userId = SecurityUtils.getCurrentUserId();

        AudioChunkResponseDTO response = chunkService.uploadChunk(meetingId, sequenceNumber, audioFile, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<AudioChunkResponseDTO>> getChunks(
            @PathVariable UUID meetingId) {

        UUID userId = SecurityUtils.getCurrentUserId();

        List<AudioChunkResponseDTO> chunks = chunkService.getChunksForMeeting(meetingId, userId);
        return ResponseEntity.ok(chunks);
    }

    @GetMapping("/progress")
    public ResponseEntity<ChunkProgressResponseDTO> getProgress(
            @PathVariable UUID meetingId) {

        UUID userId = SecurityUtils.getCurrentUserId();

        ChunkProgressResponseDTO progress = chunkService.getProgress(meetingId, userId);
        return ResponseEntity.ok(progress);
    }
}