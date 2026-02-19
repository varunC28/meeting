package com.cluely.ai.analysis.mapper;

import com.cluely.ai.analysis.dto.AnalysisResponseDTO;
import com.cluely.note.entity.NoteEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class AnalysisMapper {

    /**
     * Maps AI analysis response → list of Note entities
     * One note per: summary, action item, decision
     */
    public List<NoteEntity> toNotes(AnalysisResponseDTO response,
            UUID meetingId, UUID userId) {
        List<NoteEntity> notes = new ArrayList<>();

        // 1. Summary note
        if (response.getSummary() != null) {
            notes.add(createNote(
                    userId,
                    meetingId,
                    "SUMMARY: " + response.getSummary()));
        }

        // 2. Action item notes
        if (response.getActionItems() != null) {
            for (String actionItem : response.getActionItems()) {
                notes.add(createNote(
                        userId,
                        meetingId,
                        "ACTION ITEM: " + actionItem));
            }
        }

        // 3. Decision notes
        if (response.getDecisions() != null) {
            for (String decision : response.getDecisions()) {
                notes.add(createNote(
                        userId,
                        meetingId,
                        "DECISION: " + decision));
            }
        }

        // 4. Key topics note (combined into one)
        if (response.getKeyTopics() != null && !response.getKeyTopics().isEmpty()) {
            String topics = String.join(", ", response.getKeyTopics());
            notes.add(createNote(
                    userId,
                    meetingId,
                    "KEY TOPICS: " + topics));
        }

        return notes;
    }

    private NoteEntity createNote(UUID userId, UUID meetingId, String content) {
        NoteEntity note = new NoteEntity();
        note.setUserId(userId);
        note.setMeetingId(meetingId);
        note.setContent(content);
        note.setCreatedAt(LocalDateTime.now());
        return note;
    }
}
