package com.cluely.websocket.service;

import com.cluely.websocket.dto.ActionItemDetectedResponseDto;
import com.cluely.websocket.dto.AiSuggestionResponseDto;
import com.cluely.websocket.dto.TranscriptFragmentResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WebSocketMessagingService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketMessagingService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketMessagingService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Send transcript fragment to all users subscribed to this meeting
     */
    public void sendTranscriptFragment(TranscriptFragmentResponseDto dto) {
        String destination = "/topic/meeting." + dto.getMeetingId();
        messagingTemplate.convertAndSend(destination, dto);
        log.info("Sent transcript fragment to {}", destination);
    }

    /**
     * Send AI suggestion to specific user
     */
    public void sendAiSuggestion(UUID userId, AiSuggestionResponseDto dto) {
        String destination = "/user/" + userId + "/queue/suggestions";
        messagingTemplate.convertAndSend(destination, dto);
        log.info("Sent AI suggestion to user: {}", userId);
    }

    /**
     * Send action item detection to specific user
     */
    public void sendActionItemDetected(UUID userId, ActionItemDetectedResponseDto dto) {
        String destination = "/user/" + userId + "/queue/action-items";
        messagingTemplate.convertAndSend(destination, dto);
        log.info("Sent action item to user: {}", userId);
    }

    /**
     * Broadcast to all subscribers of a meeting topic
     */
    public void broadcastToMeeting(UUID meetingId, Object message) {
        String destination = "/topic/meeting." + meetingId;
        messagingTemplate.convertAndSend(destination, message);
        log.info("Broadcast message to meeting: {}", meetingId);
    }
}