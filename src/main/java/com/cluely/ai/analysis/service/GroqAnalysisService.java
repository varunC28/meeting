package com.cluely.ai.analysis.service;

import com.cluely.ai.analysis.dto.AnalysisRequestDTO;
import com.cluely.ai.analysis.dto.AnalysisResponseDTO;
import com.cluely.ai.analysis.exception.AnalysisException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GroqAnalysisService implements AIAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(GroqAnalysisService.class);

    private final WebClient webClient;
    private final String model;
    private final ObjectMapper objectMapper;

    public GroqAnalysisService(
            @Value("${groq.api.key}") String apiKey,
            @Value("${groq.llm.url}") String llmUrl,
            @Value("${groq.llm.model}") String model) {
        this.model = model;
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
                .baseUrl(llmUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public AnalysisResponseDTO analyzeMeeting(AnalysisRequestDTO request) {
        log.info("Starting AI analysis for meeting: {}", request.getMeetingId());

        String prompt = buildPrompt(request);

        try {
            // Build request body
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", getSystemPrompt()),
                            Map.of("role", "user", "content", prompt)),
                    "temperature", 0.3, // Low = more consistent output
                    "max_tokens", 1000,
                    "response_format", Map.of("type", "json_object"));

            // Call Groq LLM API
            Map<String, Object> groqResponse = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseGroqResponse(groqResponse);

        } catch (Exception e) {
            log.error("AI analysis failed for meeting: {}", request.getMeetingId(), e);
            throw new AnalysisException("Failed to analyze meeting transcript", e);
        }
    }

    private String getSystemPrompt() {
        return """
                You are an expert meeting analyst.
                Analyze meeting transcripts and extract structured information.
                Always respond with valid JSON only, no extra text.
                Be concise and factual.
                """;
    }

    private String buildPrompt(AnalysisRequestDTO request) {
        return String.format("""
                Analyze this meeting transcript and respond with JSON only.

                Meeting Title: %s

                Transcript:
                %s

                Respond with this exact JSON structure:
                {
                    "summary": "2-3 sentence meeting summary",
                    "actionItems": ["action item 1", "action item 2"],
                    "decisions": ["decision 1", "decision 2"],
                    "keyTopics": ["topic 1", "topic 2", "topic 3"],
                    "sentiment": "POSITIVE or NEUTRAL or NEGATIVE"
                }

                Rules:
                - summary: 2-3 sentences max
                - actionItems: specific tasks with owner if mentioned
                - decisions: concrete decisions made
                - keyTopics: 3-5 main topics discussed
                - sentiment: overall meeting tone
                - If nothing found for a field, use empty array []
                """,
                request.getMeetingTitle() != null ? request.getMeetingTitle() : "Untitled Meeting",
                request.getFullTranscript());
    }

    @SuppressWarnings("unchecked")
    private AnalysisResponseDTO parseGroqResponse(Map<String, Object> groqResponse) {
        try {
            // Extract content from Groq response
            List<Map<String, Object>> choices = (List<Map<String, Object>>) groqResponse.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");

            log.debug("Groq LLM response: {}", content);

            // Parse JSON response
            Map<String, Object> parsed = objectMapper.readValue(content, Map.class);

            AnalysisResponseDTO response = new AnalysisResponseDTO();
            response.setSummary((String) parsed.get("summary"));
            response.setActionItems((List<String>) parsed.get("actionItems"));
            response.setDecisions((List<String>) parsed.get("decisions"));
            response.setKeyTopics((List<String>) parsed.get("keyTopics"));
            response.setSentiment((String) parsed.get("sentiment"));

            return response;

        } catch (Exception e) {
            log.error("Failed to parse Groq response: {}", e.getMessage());
            throw new AnalysisException("Failed to parse AI response", e);
        }
    }
}