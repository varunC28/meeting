package com.cluely.ai.realtime;

import com.cluely.ai.analysis.service.AIAnalysisService;
import com.cluely.note.entity.NoteEntity;
import com.cluely.note.repository.NoteRepository;
import com.cluely.transcript.entity.TranscriptEntity;
import com.cluely.websocket.dto.AiSuggestionResponseDto;
import com.cluely.websocket.service.WebSocketMessagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import java.util.Map;

import java.util.List;
import java.util.UUID;

@Service
public class RealtimeAiService {

    private static final Logger log = LoggerFactory.getLogger(RealtimeAiService.class);

    private final ConversationContextManager contextManager;
    private final QuestionDetectionService questionDetectionService;
    private final NoteRepository noteRepository;
    private final WebSocketMessagingService webSocketMessagingService;
    private final AIAnalysisService aiAnalysisService;
    private final WebClient webClient;
    private final String llmModel;

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.llm.url}")
    private String groqLlmUrl;

    @Value("${groq.llm.model}")
    private String groqLlmModel;

    public RealtimeAiService(
            ConversationContextManager contextManager,
            QuestionDetectionService questionDetectionService,
            NoteRepository noteRepository,
            WebSocketMessagingService webSocketMessagingService,
            AIAnalysisService aiAnalysisService,
            @Value("${groq.api.key}") String apiKey,
            @Value("${groq.llm.url}") String llmUrl,
            @Value("${groq.llm.model}") String llmModel) {
        this.contextManager = contextManager;
        this.questionDetectionService = questionDetectionService;
        this.noteRepository = noteRepository;
        this.webSocketMessagingService = webSocketMessagingService;
        this.aiAnalysisService = aiAnalysisService;
        this.llmModel = llmModel;
        this.webClient = WebClient.builder()
                .baseUrl(llmUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Analyze transcript in real-time and generate suggestions
     */
    @Async
    public void analyzeAndSuggest(UUID meetingId, UUID userId, TranscriptEntity transcript) {
        String text = transcript.getText();

        // Check if it's a question
        if (questionDetectionService.isQuestion(text)) {
            log.info("Question detected in meeting {}: {}", meetingId, text);
            generateQuestionAnswer(meetingId, userId, text);
        }
    }

    /**
     * Generate answer to user's question
     */
    private void generateQuestionAnswer(UUID meetingId, UUID userId, String question) {
        try {
            // 1. Get recent conversation context
            String context = contextManager.getContextAsText(meetingId);

            // 2. Search past notes for relevant information
            List<NoteEntity> pastNotes = noteRepository
                    .findByMeetingIdOrderByCreatedAtDesc(meetingId);

            // 3. Build prompt for AI
            String prompt = buildQuestionPrompt(question, context, pastNotes);

            // 4. Call AI (simplified - you can use Groq LLM here)
            String answer = callAiForAnswer(prompt);

            // 5. Send suggestion via WebSocket
            AiSuggestionResponseDto suggestion = new AiSuggestionResponseDto();
            suggestion.setMeetingId(meetingId);
            suggestion.setCategory("QUESTION_ANSWER");
            suggestion.setPriority("HIGH");
            suggestion.setTitle("Answer to: " + truncate(question, 50));
            suggestion.setContent(answer);

            webSocketMessagingService.sendAiSuggestion(userId, suggestion);
            log.info("Sent AI answer to user {}", userId);

        } catch (Exception e) {
            log.error("Failed to generate answer for question: {}", question, e);
        }
    }

    /**
     * Build prompt for AI
     */
    private String buildQuestionPrompt(String question, String context,
            List<NoteEntity> pastNotes) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Recent conversation:\n");
        prompt.append(context).append("\n\n");

        if (!pastNotes.isEmpty()) {
            prompt.append("Past meeting notes:\n");
            pastNotes.stream()
                    .limit(5)
                    .forEach(note -> prompt.append("- ").append(note.getContent()).append("\n"));
            prompt.append("\n");
        }

        prompt.append("Question: ").append(question).append("\n\n");
        prompt.append("Provide a concise answer based on the above context. ");
        prompt.append("If no relevant information found, say 'No information found in recent context.'");

        return prompt.toString();
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    private String callAiForAnswer(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", llmModel,
                    "messages", List.of(
                            Map.of("role", "system", "content",
                                    "You are a helpful meeting assistant. Provide concise, factual answers."),
                            Map.of("role", "user", "content", prompt)),
                    "temperature", 0.3,
                    "max_tokens", 200 // Short answers for real-time
            );

            Map<String, Object> response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // Extract answer from response
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

            return (String) message.get("content");

        } catch (Exception e) {
            log.error("Failed to get AI answer: {}", e.getMessage());
            return "Unable to generate answer at this time.";
        }
    }

    /**
     * Truncate text to max length
     */
    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}