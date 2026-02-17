package com.cluely.meeting.service;

import com.cluely.audio_chunks.exception.InvalidMeetingStateException;
import com.cluely.audio_chunks.service.ChunkProcessingService;
import com.cluely.global.NotFoundException;
import com.cluely.meeting.dto.MeetingCreateRequestDto;
import com.cluely.meeting.dto.MeetingDashboardDto;
import com.cluely.meeting.dto.MeetingResponseDto;
import com.cluely.meeting.dto.MeetingUpdateRequestDto;
import com.cluely.meeting.entity.MeetingEntity;
import com.cluely.meeting.entity.MeetingStatus;
import com.cluely.meeting.mapper.MeetingMapper;
import com.cluely.meeting.repository.MeetingRepository;

import com.cluely.note.dto.NoteResponseDto;
import com.cluely.note.mapper.NoteMapper;
import com.cluely.note.repository.NoteRepository;

import com.cluely.transcript.dto.TranscriptResponseDto;
import com.cluely.transcript.mapper.TranscriptMapper;
import com.cluely.transcript.repository.TranscriptRepository;

import com.cluely.user.entity.UserEntity;
import com.cluely.user.mapper.UserMapper;
import com.cluely.user.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.cluely.meeting.spec.MeetingSpecifications.*;
import static com.cluely.security.SecurityUtils.getCurrentUserId;

@Service
@Transactional
public class MeetingService {

        private final MeetingRepository repository;
        private final NoteRepository noteRepository;
        private final TranscriptRepository transcriptRepository;
        private final UserRepository userRepository;
        private final ChunkProcessingService chunkProcessingService;

        public MeetingService(
                        MeetingRepository repository,
                        NoteRepository noteRepository,
                        TranscriptRepository transcriptRepository,
                        UserRepository userRepository,
                        ChunkProcessingService chunkProcessingService) {

                this.repository = repository;
                this.noteRepository = noteRepository;
                this.transcriptRepository = transcriptRepository;
                this.userRepository = userRepository;
                this.chunkProcessingService = chunkProcessingService;
        }

        // ✅ Filtering + pagination
        public Page<MeetingResponseDto> getMeetingsFiltered(
                        String title,
                        String source,
                        LocalDateTime from,
                        LocalDateTime to,
                        Pageable pageable) {
                UUID currentUser = getCurrentUserId();

                Specification<MeetingEntity> spec = Specification.where(notDeleted())
                                .and(hasUser(currentUser))
                                .and(titleContains(title))
                                .and(hasSource(source))
                                .and(createdAfter(from))
                                .and(createdBefore(to));

                return repository.findAll(spec, pageable)
                                .map(MeetingMapper::toResponse);
        }

        // ✅ Create meeting
        public MeetingResponseDto createMeeting(MeetingCreateRequestDto dto) {

                MeetingEntity entity = MeetingMapper.toEntity(dto);

                entity.setUserId(getCurrentUserId()); // REQUIRED

                MeetingEntity saved = repository.save(entity);
                return MeetingMapper.toResponse(saved);
        }

        // ✅ Get or throw
        public MeetingEntity getOrThrow(UUID id) {
                return repository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Meeting not found: " + id));
        }

        public MeetingDashboardDto getDashboard(UUID meetingId, Pageable pageable) {

                UUID userId = getCurrentUserId();

                MeetingEntity meeting = repository.findByMeetingIdAndUserIdAndDeletedFalse(
                                meetingId, userId)
                                .orElseThrow(() -> new NotFoundException("Meeting not found"));

                UserEntity user = userRepository
                                .findById(meeting.getUserId())
                                .orElseThrow(() -> new NotFoundException("User not found"));

                Page<NoteResponseDto> notes = noteRepository
                                .findByMeetingIdAndDeletedFalse(meetingId, pageable)
                                .map(NoteMapper::toResponse);

                Page<TranscriptResponseDto> transcripts = transcriptRepository
                                .findByMeetingIdAndDeletedFalse(meetingId, pageable)
                                .map(TranscriptMapper::toResponse);

                return new MeetingDashboardDto(
                                MeetingMapper.toResponse(meeting),
                                UserMapper.toResponse(user),
                                notes,
                                transcripts);
        }

        // ✅ Update meeting
        public MeetingResponseDto updateMeeting(UUID id, MeetingUpdateRequestDto dto) {

                UUID userId = getCurrentUserId();

                MeetingEntity meeting = repository.findByMeetingIdAndUserIdAndDeletedFalse(id, userId)
                                .orElseThrow(() -> new NotFoundException("Meeting not found"));

                meeting.setTitle(dto.getTitle());
                meeting.setSource(dto.getSource());

                return MeetingMapper.toResponse(repository.save(meeting));
        }

        // Delete Meeting
        public void deleteMeeting(UUID id) {

                UUID userId = getCurrentUserId();

                MeetingEntity meeting = repository.findByMeetingIdAndUserIdAndDeletedFalse(id, userId)
                                .orElseThrow(() -> new NotFoundException("Meeting not found"));

                meeting.setDeleted(true);
                meeting.setDeletedAt(LocalDateTime.now());

                repository.save(meeting);
        }

        // Get My Meetings
        public Page<MeetingResponseDto> getMyMeetings(Pageable pageable) {

                UUID userId = getCurrentUserId();

                return repository
                                .findByUserIdAndDeletedFalse(userId, pageable)
                                .map(MeetingMapper::toResponse);
        }

        // Get Single meeting
        public MeetingResponseDto getMyMeeting(UUID meetingId) {

                UUID userId = getCurrentUserId();

                MeetingEntity meeting = repository.findByMeetingIdAndUserIdAndDeletedFalse(
                                meetingId, userId)
                                .orElseThrow(() -> new NotFoundException("Meeting not found"));

                return MeetingMapper.toResponse(meeting);
        }

        // Start a Meeting
        public void startMeeting(UUID meetingId) {

                UUID userId = getCurrentUserId();

                MeetingEntity meeting = repository
                                .findByMeetingIdAndUserIdAndDeletedFalse(meetingId, userId)
                                .orElseThrow(() -> new NotFoundException("Meeting not found"));

                if (meeting.getStatus() != MeetingStatus.SCHEDULED) {
                        throw new IllegalStateException(
                                        "Only SCHEDULED meetings can be started. Current status: "
                                                        + meeting.getStatus());
                }

                meeting.setStatus(MeetingStatus.LIVE);
                meeting.setStartedAt(LocalDateTime.now());

        }

        // End a Meeting
        // public void endMeeting(UUID meetingId) {

        // UUID userId = getCurrentUserId();

        // MeetingEntity meeting = repository
        // .findByMeetingIdAndUserIdAndDeletedFalse(meetingId, userId)
        // .orElseThrow(() -> new NotFoundException("Meeting not found"));

        // if (meeting.getStatus() != MeetingStatus.LIVE) {
        // throw new IllegalStateException(
        // "Only LIVE meetings can be completed. Current status: " +
        // meeting.getStatus());
        // }

        // meeting.setStatus(MeetingStatus.COMPLETED);
        // meeting.setEndedAt(LocalDateTime.now());
        // }

        public void endMeeting(UUID meetingId) {

                UUID userId = getCurrentUserId();

                MeetingEntity meeting = repository
                                .findByMeetingIdAndUserIdAndDeletedFalse(meetingId, userId)
                                .orElseThrow(() -> new NotFoundException("Meeting not found"));

                if (meeting.getStatus() != MeetingStatus.LIVE) {
                        throw new InvalidMeetingStateException(
                                        "Only LIVE meetings can be completed. Current status: " +
                                                        meeting.getStatus());
                }

                meeting.setStatus(MeetingStatus.PROCESSING);
                meeting.setEndedAt(LocalDateTime.now());

                chunkProcessingService.processMeetingChunksAsync(meeting);
        }

}
