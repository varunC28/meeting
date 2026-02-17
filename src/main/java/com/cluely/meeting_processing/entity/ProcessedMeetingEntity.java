package com.cluely.meeting_processing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "processed_meetings")
public class ProcessedMeetingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "processed_meeting_id")
    private UUID processedMeetingId;

    @Column(name = "meeting_id", nullable = false, unique = true)
    private UUID meetingId;

    @Column(name = "full_audio_path", nullable = false, length = 500)
    private String fullAudioPath;

    @Column(name = "total_duration_ms")
    private Long totalDurationMs;

    @Column(name = "chunk_count", nullable = false)
    private Integer chunkCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 20)
    private ProcessingStatus processingStatus;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    // Constructors
    public ProcessedMeetingEntity() {
        this.processedAt = LocalDateTime.now();
        this.processingStatus = ProcessingStatus.PENDING;
    }

    // Getters and Setters
    public UUID getProcessedMeetingId() {
        return processedMeetingId;
    }

    public void setProcessedMeetingId(UUID processedMeetingId) {
        this.processedMeetingId = processedMeetingId;
    }

    public UUID getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(UUID meetingId) {
        this.meetingId = meetingId;
    }

    public String getFullAudioPath() {
        return fullAudioPath;
    }

    public void setFullAudioPath(String fullAudioPath) {
        this.fullAudioPath = fullAudioPath;
    }

    public Long getTotalDurationMs() {
        return totalDurationMs;
    }

    public void setTotalDurationMs(Long totalDurationMs) {
        this.totalDurationMs = totalDurationMs;
    }

    public Integer getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(Integer chunkCount) {
        this.chunkCount = chunkCount;
    }

    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(ProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}