package com.cluely.ai.config;

import com.cluely.ai.analysis.service.AIAnalysisService;
import com.cluely.ai.analysis.service.GroqAnalysisService;
import com.cluely.ai.speech.service.SpeechToTextService;
import com.cluely.ai.speech.groq.GroqWhisperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    private static final Logger log = LoggerFactory.getLogger(AIConfig.class);

    @Value("${cluely.ai.speech-provider:groq}")
    private String speechProvider;

    @Value("${cluely.ai.analysis-provider:groq}")
    private String analysisProvider;

    @Bean
    public SpeechToTextService speechToTextService(GroqWhisperService groqWhisperService) {
        log.info("Using speech-to-text provider: {}", speechProvider);
        switch (speechProvider.toLowerCase()) {
            case "groq":
                return groqWhisperService;
            default:
                log.warn("Unknown provider: {}, defaulting to Groq", speechProvider);
                return groqWhisperService;
        }
    }

    @Bean
    public AIAnalysisService aiAnalysisService(GroqAnalysisService groqAnalysisService) {
        log.info("Using AI analysis provider: {}", analysisProvider);
        switch (analysisProvider.toLowerCase()) {
            case "groq":
                return groqAnalysisService;
            default:
                log.warn("Unknown provider: {}, defaulting to Groq", analysisProvider);
                return groqAnalysisService;
        }
    }
}