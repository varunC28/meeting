package com.cluely.audio_chunks.service;

import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.cluely.audio_chunks.entity.AudioChunkEntity;
import com.cluely.audio_chunks.repository.AudioChunkRepository;
import com.cluely.meeting.entity.MeetingEntity;
import com.cluely.meeting.entity.MeetingStatus;
import com.cluely.meeting.repository.MeetingRepository;
import com.cluely.meeting_processing.entity.ProcessedMeetingEntity;
import com.cluely.meeting_processing.service.ChunkAssemblyService;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ChunkProcessingService {

    private static final Logger log = LoggerFactory.getLogger(ChunkProcessingService.class);

    private final AudioChunkRepository chunkRepository;
    private final MeetingRepository meetingRepository;
    private final ChunkAssemblyService chunkAssemblyService;

    public ChunkProcessingService(
            AudioChunkRepository chunkRepository,
            MeetingRepository meetingRepository,
            ChunkAssemblyService chunkAssemblyService) { // Add this
        this.chunkRepository = chunkRepository;
        this.meetingRepository = meetingRepository;
        this.chunkAssemblyService = chunkAssemblyService;
    }

    @Async
    public void processMeetingChunksAsync(MeetingEntity meeting) {

        // Re-fetch a managed entity inside this transaction
        MeetingEntity managedMeeting = meetingRepository
                .findById(meeting.getMeetingId())
                .orElseThrow(() -> new IllegalStateException("Meeting not found in async processing"));

        List<AudioChunkEntity> chunks = chunkRepository
                .findByMeetingIdOrderBySequenceNumberAsc(managedMeeting.getMeetingId());

        if (chunks.isEmpty()) {
            managedMeeting.setStatus(MeetingStatus.FAILED);
            meetingRepository.save(managedMeeting);
            return;
        }

        boolean validSequence = validateSequence(chunks);

        if (!validSequence) {
            managedMeeting.setStatus(MeetingStatus.FAILED);
            meetingRepository.save(managedMeeting);
            return;
        }

        try {
            ProcessedMeetingEntity processed = chunkAssemblyService.assembleChunks(chunks);
            managedMeeting.setStatus(MeetingStatus.COMPLETED);
            log.info("Meeting {} processed successfully. Output: {}",
                    managedMeeting.getMeetingId(), processed.getFullAudioPath());
        } catch (Exception e) {
            managedMeeting.setStatus(MeetingStatus.FAILED);
            log.error("Failed to process meeting {}", managedMeeting.getMeetingId(), e);
        }

        managedMeeting.setStatus(MeetingStatus.COMPLETED);
        meetingRepository.save(managedMeeting);
    }

    @Transactional
    public void updateMeetingStatus(UUID meetingId, MeetingStatus status) {
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalStateException("Meeting not found"));
        meeting.setStatus(status);
        meetingRepository.save(meeting);
    }

    private boolean validateSequence(List<AudioChunkEntity> chunks) {
        for (int i = 0; i < chunks.size(); i++) {
            if (!chunks.get(i).getSequenceNumber().equals(i + 1)) {
                return false;
            }
        }
        return true;
    }

}
