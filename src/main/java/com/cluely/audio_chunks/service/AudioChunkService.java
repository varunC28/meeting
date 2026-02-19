package com.cluely.audio_chunks.service;

import com.cluely.ai.speech.service.TranscriptionService;
import com.cluely.audio_chunks.dto.AudioChunkResponseDTO;
import com.cluely.audio_chunks.dto.ChunkProgressResponseDTO;
import com.cluely.audio_chunks.entity.AudioChunkEntity;
import com.cluely.audio_chunks.entity.ChunkStatus;
import com.cluely.audio_chunks.exception.DuplicateChunkException;
import com.cluely.audio_chunks.exception.InvalidMeetingStateException;
import com.cluely.audio_chunks.mapper.AudioChunkMapper;
import com.cluely.audio_chunks.repository.AudioChunkRepository;
import com.cluely.global.NotFoundException;
import com.cluely.meeting.entity.MeetingEntity;
import com.cluely.meeting.entity.MeetingStatus;
import com.cluely.meeting.repository.MeetingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AudioChunkService {

    private final AudioChunkRepository chunkRepository;
    private final MeetingRepository meetingRepository;
    private final FileStorageService fileStorageService;
    private final AudioChunkMapper mapper;
    private final TranscriptionService transcriptionService;

    public AudioChunkService(
            AudioChunkRepository chunkRepository,
            MeetingRepository meetingRepository,
            FileStorageService fileStorageService,
            AudioChunkMapper mapper,
            TranscriptionService transcriptionService) {
        this.chunkRepository = chunkRepository;
        this.meetingRepository = meetingRepository;
        this.fileStorageService = fileStorageService;
        this.mapper = mapper;
        this.transcriptionService = transcriptionService;
    }

    public AudioChunkResponseDTO uploadChunk(UUID meetingId, Integer sequenceNumber,
            MultipartFile file, UUID userId) {
        // 1. Verify meeting exists and belongs to user
        MeetingEntity meeting = meetingRepository
                .findByMeetingIdAndUserIdAndDeletedFalse(meetingId, userId)
                .orElseThrow(() -> new NotFoundException("Meeting not found or access denied"));

        // 2. Verify meeting is in LIVE status
        if (meeting.getStatus() != MeetingStatus.LIVE) {
            throw new InvalidMeetingStateException(
                    "Meeting must be LIVE to accept chunks. Current status: "
                            + meeting.getStatus());
        }

        // 3. Check for duplicate chunk
        if (chunkRepository.existsByMeetingIdAndSequenceNumber(meetingId, sequenceNumber)) {
            throw new DuplicateChunkException(
                    "Chunk with sequence number " + sequenceNumber + " already exists");
        }

        // 4. Store file on disk
        String filePath = fileStorageService.storeChunk(meetingId, sequenceNumber, file);

        // 5. Create and save entity
        AudioChunkEntity chunk = new AudioChunkEntity();
        chunk.setMeeting(meeting);
        chunk.setSequenceNumber(sequenceNumber);
        chunk.setFilePath(filePath);
        chunk.setSizeBytes(file.getSize());
        chunk.setCreatedAt(LocalDateTime.now());
        chunk.setStatus(ChunkStatus.UPLOADED);
        chunk.setMimeType(file.getContentType());

        long estimatedDurationMs = (file.getSize() / 16000) * 1000;
        chunk.setDurationMs((int) estimatedDurationMs);

        AudioChunkEntity saved = chunkRepository.save(chunk);

        // 6. Trigger async transcription (real-time pipeline)
        transcriptionService.transcribeChunkAsync(saved.getChunkId());

        return mapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<AudioChunkResponseDTO> getChunksForMeeting(UUID meetingId, UUID userId) {
        meetingRepository.findByMeetingIdAndUserIdAndDeletedFalse(meetingId, userId)
                .orElseThrow(() -> new NotFoundException("Meeting not found or access denied"));

        List<AudioChunkEntity> chunks = chunkRepository
                .findByMeetingIdOrderBySequenceNumberAsc(meetingId);
        return mapper.toResponseDTOList(chunks);
    }

    @Transactional(readOnly = true)
    public ChunkProgressResponseDTO getProgress(UUID meetingId, UUID userId) {
        MeetingEntity meeting = meetingRepository
                .findByMeetingIdAndUserIdAndDeletedFalse(meetingId, userId)
                .orElseThrow(() -> new NotFoundException("Meeting not found or access denied"));

        long totalChunks = chunkRepository.countByMeetingIdAndStatus(
                meetingId, ChunkStatus.UPLOADED);
        Integer maxSequence = chunkRepository.findMaxSequenceNumber(meetingId).orElse(0);

        return mapper.toProgressDTO(totalChunks, maxSequence, meeting.getStatus());
    }
}