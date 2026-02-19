package com.cluely.ai.speech.groq;

import com.cluely.ai.speech.service.SpeechToTextService;
import com.cluely.ai.speech.dto.TranscriptionRequestDTO;
import com.cluely.ai.speech.dto.TranscriptionResponseDTO;
import com.cluely.ai.speech.exception.TranscriptionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;

import java.util.Map;

@Service
public class GroqWhisperService implements SpeechToTextService {

    private static final Logger log = LoggerFactory.getLogger(GroqWhisperService.class);

    private final WebClient webClient;
    private final String model;

    public GroqWhisperService(
            @Value("${groq.api.key}") String apiKey,
            @Value("${groq.api.url}") String apiUrl,
            @Value("${groq.whisper.model}") String model) {

        this.model = model;
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    @Override
    public TranscriptionResponseDTO transcribeChunk(TranscriptionRequestDTO request) {
        log.info("Transcribing chunk {} for meeting: {}",
                request.getSequenceNumber(), request.getMeetingId());
        return callGroqAPI(request);
    }

    @Override
    public TranscriptionResponseDTO transcribeFullAudio(TranscriptionRequestDTO request) {
        log.info("Transcribing full audio for meeting: {}", request.getMeetingId());
        return callGroqAPI(request);
    }

    // @Retryable(
    // retryFor = { Exception.class },
    // maxAttempts = 3,
    // backoff = @Backoff(delay = 1000, multiplier = 2) // 1s, 2s, 4s
    // )

    private TranscriptionResponseDTO callGroqAPI(TranscriptionRequestDTO request) {
        // Build multipart form data
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("file", new FileSystemResource(request.getAudioFilePath().toFile()));
        formData.add("model", model);
        formData.add("language", request.getLanguage());
        formData.add("response_format", "verbose_json"); // Get timestamps + confidence

        try {
            // Call Groq API
            Map<String, Object> groqResponse = webClient.post()
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(formData))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(); // Synchronous for now (async in Phase 4)

            return mapGroqResponse(groqResponse, request);

        } catch (Exception e) {
            log.error("Groq API call failed for meeting: {}", request.getMeetingId(), e);
            throw new TranscriptionException("Failed to transcribe audio", e);
        }
    }

    @Recover
    public TranscriptionResponseDTO recoverTranscription(
            Exception e, TranscriptionRequestDTO request) {
        log.error("All 3 transcription attempts failed for meeting: {}. Error: {}",
                request.getMeetingId(), e.getMessage());
        throw new TranscriptionException("Transcription failed after 3 attempts", e);
    }

    private TranscriptionResponseDTO mapGroqResponse(Map<String, Object> groqResponse,
            TranscriptionRequestDTO request) {
        TranscriptionResponseDTO response = new TranscriptionResponseDTO();
        response.setMeetingId(request.getMeetingId());
        response.setSequenceNumber(request.getSequenceNumber());
        response.setLanguage((String) groqResponse.get("language"));
        response.setText((String) groqResponse.get("text"));

        // Extract timing from first/last segment
        if (groqResponse.containsKey("segments")) {
            java.util.List<Map<String, Object>> segments = (java.util.List<Map<String, Object>>) groqResponse
                    .get("segments");

            if (!segments.isEmpty()) {
                Map<String, Object> firstSegment = segments.get(0);
                Map<String, Object> lastSegment = segments.get(segments.size() - 1);

                response.setStartTime(toDouble(firstSegment.get("start")));
                response.setEndTime(toDouble(lastSegment.get("end")));

                double avgLogProb = segments.stream()
                        .filter(s -> s.containsKey("avg_logprob"))
                        .mapToDouble(s -> toDouble(s.get("avg_logprob")))
                        .average()
                        .orElse(-1.0);

                // Convert log probability to 0-1 scale
                response.setConfidence(Math.exp(avgLogProb));
            }
        }

        return response;
    }

    private Double toDouble(Object value) {
        if (value == null)
            return null;
        if (value instanceof Double)
            return (Double) value;
        if (value instanceof Float)
            return ((Float) value).doubleValue();
        if (value instanceof Integer)
            return ((Integer) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}