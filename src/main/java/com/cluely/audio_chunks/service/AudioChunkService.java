package com.cluely.audio_chunks.service;

import com.cluely.audio_chunks.entity.AudioChunkEntity;
import com.cluely.audio_chunks.entity.ChunkStatus;
import com.cluely.audio_chunks.exception.DuplicateChunkException;
import com.cluely.audio_chunks.exception.InvalidMeetingStateException;
import com.cluely.audio_chunks.exception.ResourceNotFoundException;
import com.cluely.audio_chunks.repository.AudioChunkRepository;
import com.cluely.meeting.entity.MeetingEntity;
import com.cluely.meeting.entity.MeetingStatus;
import com.cluely.meeting.repository.MeetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AudioChunkService {

    private final AudioChunkRepository chunkRepository;
    private final MeetingRepository meetingRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public AudioChunkService(
            AudioChunkRepository chunkRepository,
            MeetingRepository meetingRepository,
            FileStorageService fileStorageService) {
        this.chunkRepository = chunkRepository;
        this.meetingRepository = meetingRepository;
        this.fileStorageService = fileStorageService;
    }

    public AudioChunkEntity uploadChunk(UUID meetingId, Integer sequenceNumber, MultipartFile file, UUID userId) {
        // 1. Verify meeting exists and belongs to user
        MeetingEntity meeting = meetingRepository.findByMeetingIdAndUserIdAndDeletedFalse(meetingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found or access denied"));

        // 2. Verify meeting is in LIVE status
        if (meeting.getStatus() != MeetingStatus.LIVE) {
            throw new InvalidMeetingStateException(
                    "Meeting must be LIVE to accept chunks. Current status: " + meeting.getStatus());
        }

        // 3. Check for duplicate chunk
        if (chunkRepository.existsByMeetingIdAndSequenceNumber(meetingId, sequenceNumber)) {
            throw new DuplicateChunkException("Chunk with sequence number " + sequenceNumber + " already exists");
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

        return chunkRepository.save(chunk);
    }
}