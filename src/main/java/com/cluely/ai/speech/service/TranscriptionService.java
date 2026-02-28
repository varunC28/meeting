package com.cluely.ai.speech.service;

import com.cluely.ai.analysis.service.MeetingAnalysisService;
import com.cluely.ai.realtime.ConversationContextManager;
import com.cluely.ai.realtime.RealtimeAiService;
import com.cluely.ai.speech.dto.TranscriptionRequestDTO;
import com.cluely.ai.speech.dto.TranscriptionResponseDTO;
import com.cluely.ai.speech.mapper.TranscriptionMapper;
import com.cluely.audio_chunks.entity.AudioChunkEntity;
import com.cluely.audio_chunks.entity.ChunkStatus;
import com.cluely.audio_chunks.repository.AudioChunkRepository;
import com.cluely.transcript.entity.TranscriptEntity;
import com.cluely.transcript.repository.TranscriptRepository;
import com.cluely.websocket.dto.TranscriptFragmentResponseDto;
import com.cluely.websocket.mapper.WebSocketMapper;
import com.cluely.websocket.service.WebSocketMessagingService;
import com.cluely.ai.realtime.ConversationContextManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Paths;
import java.util.UUID;

@Service
public class TranscriptionService {

    private static final Logger log = LoggerFactory.getLogger(TranscriptionService.class);

    private final SpeechToTextService speechToTextService;
    private final TranscriptionMapper transcriptionMapper;
    private final TranscriptRepository transcriptRepository;
    private final AudioChunkRepository chunkRepository;
    private final MeetingAnalysisService meetingAnalysisService;
    private final WebSocketMessagingService webSocketMessagingService;
    private final WebSocketMapper webSocketMapper;
    private final ConversationContextManager contextManager;
    private final RealtimeAiService realtimeAiService;

    public TranscriptionService(
            SpeechToTextService speechToTextService,
            TranscriptionMapper transcriptionMapper,
            TranscriptRepository transcriptRepository,
            AudioChunkRepository chunkRepository,
            MeetingAnalysisService meetingAnalysisService,
            WebSocketMessagingService webSocketMessagingService,
            WebSocketMapper webSocketMapper,
            ConversationContextManager contextManager,
            RealtimeAiService realtimeAiService) {
        this.speechToTextService = speechToTextService;
        this.transcriptionMapper = transcriptionMapper;
        this.transcriptRepository = transcriptRepository;
        this.chunkRepository = chunkRepository;
        this.meetingAnalysisService = meetingAnalysisService;
        this.webSocketMessagingService = webSocketMessagingService;
        this.webSocketMapper = webSocketMapper;
        this.contextManager = contextManager;
        this.realtimeAiService = realtimeAiService;
    }

    /**
     * Real-time pipeline: transcribe individual chunk
     */
    @Async
    @Transactional
    public void transcribeChunkAsync(UUID chunkId) {
        log.info("Starting transcription for chunk: {}", chunkId);

        AudioChunkEntity chunk = chunkRepository.findById(chunkId)
                .orElseThrow(() -> new IllegalStateException(
                        "Chunk not found: " + chunkId));

        try {
            // Build request
            TranscriptionRequestDTO request = new TranscriptionRequestDTO(
                    chunk.getMeeting().getMeetingId(),
                    Paths.get(chunk.getFilePath()),
                    chunk.getSequenceNumber());

            request.setDetectSpeakers(true);

            // Call Groq Whisper
            TranscriptionResponseDTO response = speechToTextService.transcribeChunk(request);

            // Fix offset
            if (chunk.getDurationMs() != null && chunk.getSequenceNumber() > 1) {
                double offsetSeconds = (chunk.getSequenceNumber() - 1) *
                        (chunk.getDurationMs() / 1000.0);
                if (response.getStartTime() != null) {
                    response.setStartTime(response.getStartTime() + offsetSeconds);
                }
                if (response.getEndTime() != null) {
                    response.setEndTime(response.getEndTime() + offsetSeconds);
                }
            }

            // Save transcript fragment
            TranscriptEntity transcript = transcriptionMapper.toEntity(response);
            transcriptRepository.save(transcript);
            log.info("Saved transcript fragment for chunk: {}", chunkId);

            // Send via WebSocket
            TranscriptFragmentResponseDto wsMessage = webSocketMapper.toResponse(transcript);
            webSocketMessagingService.sendTranscriptFragment(wsMessage);

            // Add to conversation context for AI analysis
            contextManager.addTranscript(chunk.getMeeting().getMeetingId(), transcript);

            // Analyze for real-time suggestions
            realtimeAiService.analyzeAndSuggest(
                    chunk.getMeeting().getMeetingId(),
                    chunk.getMeeting().getUserId(),
                    transcript);

            // Update chunk status
            chunk.setStatus(ChunkStatus.PROCESSED);
            chunkRepository.save(chunk);
            log.info("Chunk {} marked as PROCESSED", chunkId);

        } catch (Exception e) {
            log.error("Transcription failed for chunk: {}", chunkId, e);
            chunk.setStatus(ChunkStatus.FAILED);
            chunkRepository.save(chunk);
        }
    }

    /**
     * Post-meeting pipeline: transcribe full audio
     * Then trigger AI analysis
     */
    @Async
    @Transactional
    public void transcribeFullAudioAsync(UUID meetingId, String fullAudioPath) {
        log.info("Starting full audio transcription for meeting: {}", meetingId);

        try {
            // Build request
            TranscriptionRequestDTO request = new TranscriptionRequestDTO(
                    meetingId,
                    Paths.get(fullAudioPath),
                    null // null = full audio
            );

            // Call Groq Whisper
            TranscriptionResponseDTO response = speechToTextService
                    .transcribeFullAudio(request);

            // Save full transcript
            TranscriptEntity transcript = transcriptionMapper.toEntity(response);
            transcriptRepository.save(transcript);
            log.info("Saved full transcript for meeting: {}", meetingId);

            // Trigger AI analysis (next step in pipeline)
            meetingAnalysisService.analyzeMeetingAsync(meetingId);
            log.info("AI analysis triggered for meeting: {}", meetingId);

        } catch (Exception e) {
            log.error("Full audio transcription failed for meeting: {}", meetingId, e);
        }
    }
}