package com.cluely.ai.realtime;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class QuestionDetectionService {

    // Question patterns
    private static final Pattern QUESTION_MARK = Pattern.compile(".*\\?\\s*$");
    private static final Pattern QUESTION_WORDS = Pattern.compile(
            "^(what|when|where|who|why|how|did|does|do|is|are|was|were|can|could|would|should)\\b",
            Pattern.CASE_INSENSITIVE);

    // Detects if text is a question

    public boolean isQuestion(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String trimmed = text.trim();

        // Check for question mark
        if (QUESTION_MARK.matcher(trimmed).matches()) {
            return true;
        }

        // Check for question words at start
        if (QUESTION_WORDS.matcher(trimmed).find()) {
            return true;
        }

        return false;
    }

    /**
     * Extract question type for better AI response
     */
    public String getQuestionType(String text) {
        if (text == null)
            return "UNKNOWN";

        String lower = text.toLowerCase().trim();

        if (lower.startsWith("what"))
            return "WHAT";
        if (lower.startsWith("when"))
            return "WHEN";
        if (lower.startsWith("where"))
            return "WHERE";
        if (lower.startsWith("who"))
            return "WHO";
        if (lower.startsWith("why"))
            return "WHY";
        if (lower.startsWith("how"))
            return "HOW";

        return "YES_NO";
    }
}