package com.cluely.ai.realtime;

import com.cluely.transcript.entity.TranscriptEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationContextManager {

    private static final int MAX_CONTEXT_SIZE = 10;

    // meetingId -> list of recent transcripts
    private final Map<UUID, LinkedList<TranscriptEntity>> conversationBuffer;

    public ConversationContextManager() {
        this.conversationBuffer = new ConcurrentHashMap<>();
    }

    /**
     * Add new transcript to conversation context
     */
    public void addTranscript(UUID meetingId, TranscriptEntity transcript) {
        conversationBuffer.computeIfAbsent(meetingId, k -> new LinkedList<>());

        LinkedList<TranscriptEntity> context = conversationBuffer.get(meetingId);
        context.addLast(transcript);

        // Keep only last 10 messages
        if (context.size() > MAX_CONTEXT_SIZE) {
            context.removeFirst();
        }
    }

    /**
     * Get recent conversation context for AI analysis
     */
    public List<TranscriptEntity> getContext(UUID meetingId) {
        return conversationBuffer.getOrDefault(meetingId, new LinkedList<>());
    }

    /**
     * Get last N messages
     */
    public List<TranscriptEntity> getLastN(UUID meetingId, int count) {
        LinkedList<TranscriptEntity> context = conversationBuffer.get(meetingId);
        if (context == null || context.isEmpty()) {
            return Collections.emptyList();
        }

        int start = Math.max(0, context.size() - count);
        return new ArrayList<>(context.subList(start, context.size()));
    }

    /**
     * Get context as readable text for AI
     */
    public String getContextAsText(UUID meetingId) {
        List<TranscriptEntity> context = getContext(meetingId);
        if (context.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (TranscriptEntity t : context) {
            sb.append("[").append(t.getSpeaker()).append("]: ")
                    .append(t.getText()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Clear context when meeting ends
     */
    public void clearContext(UUID meetingId) {
        conversationBuffer.remove(meetingId);
    }

    /**
     * Get size of current context
     */
    public int getContextSize(UUID meetingId) {
        LinkedList<TranscriptEntity> context = conversationBuffer.get(meetingId);
        return context != null ? context.size() : 0;
    }
}