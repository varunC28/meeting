package com.cluely.ai.analysis.service;

import com.cluely.ai.analysis.dto.AnalysisRequestDTO;
import com.cluely.ai.analysis.dto.AnalysisResponseDTO;
import com.cluely.ai.analysis.mapper.AnalysisMapper;
import com.cluely.meeting.entity.MeetingEntity;
import com.cluely.meeting.repository.MeetingRepository;
import com.cluely.note.entity.NoteEntity;
import com.cluely.note.repository.NoteRepository;
import com.cluely.transcript.entity.TranscriptEntity;
import com.cluely.transcript.repository.TranscriptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MeetingAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(MeetingAnalysisService.class);

    private final AIAnalysisService aiAnalysisService;
    private final AnalysisMapper analysisMapper;
    private final TranscriptRepository transcriptRepository;
    private final MeetingRepository meetingRepository;
    private final NoteRepository noteRepository;

    public MeetingAnalysisService(
            AIAnalysisService aiAnalysisService,
            AnalysisMapper analysisMapper,
            TranscriptRepository transcriptRepository,
            MeetingRepository meetingRepository,
            NoteRepository noteRepository) {
        this.aiAnalysisService = aiAnalysisService;
        this.analysisMapper = analysisMapper;
        this.transcriptRepository = transcriptRepository;
        this.meetingRepository = meetingRepository;
        this.noteRepository = noteRepository;
    }

    /**
     * Triggered after full audio transcription completes
     * Assembles transcript fragments → analyzes → saves notes
     */
    @Async
    @Transactional
    public void analyzeMeetingAsync(UUID meetingId) {
        log.info("Starting meeting analysis for: {}", meetingId);

        try {
            // 1. Load meeting
            MeetingEntity meeting = meetingRepository.findById(meetingId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Meeting not found: " + meetingId));

            // 2. Load all transcript fragments for this meeting
            List<TranscriptEntity> fragments = transcriptRepository
                    .findByMeetingIdAndDeletedFalse(
                            meetingId,
                            PageRequest.of(0, 1000)) // Max 1000 fragments
                    .getContent();

            if (fragments.isEmpty()) {
                log.warn("No transcript fragments found for meeting: {}", meetingId);
                return;
            }

            // 3. Assemble fragments into full transcript text
            String fullTranscript = assembleTranscript(fragments);
            log.info("Assembled transcript: {} characters", fullTranscript.length());

            // 4. Build analysis request
            AnalysisRequestDTO request = new AnalysisRequestDTO(
                    meetingId,
                    meeting.getUserId(),
                    fullTranscript,
                    meeting.getTitle());

            // 5. Call AI analysis
            AnalysisResponseDTO response = aiAnalysisService.analyzeMeeting(request);
            log.info("AI analysis completed for meeting: {}", meetingId);

            // 6. Map response → Note entities
            List<NoteEntity> notes = analysisMapper.toNotes(
                    response,
                    meetingId,
                    meeting.getUserId());

            // 7. Save all notes
            noteRepository.saveAll(notes);
            log.info("Saved {} notes for meeting: {}", notes.size(), meetingId);

        } catch (Exception e) {
            log.error("Meeting analysis failed for: {}", meetingId, e);
        }
    }

    /**
     * Assembles transcript fragments into single text
     * Orders by start_time, adds speaker labels
     */
    private String assembleTranscript(List<TranscriptEntity> fragments) {
        return fragments.stream()
                .sorted((a, b) -> {
                    if (a.getStartTime() == null)
                        return -1;
                    if (b.getStartTime() == null)
                        return 1;
                    return Double.compare(a.getStartTime(), b.getStartTime());
                })
                .map(fragment -> {
                    String speaker = fragment.getSpeaker() != null
                            ? fragment.getSpeaker()
                            : "Unknown";
                    return String.format("[%s]: %s", speaker, fragment.getText());
                })
                .collect(Collectors.joining("\n"));
    }
}