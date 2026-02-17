package com.cluely.audio_chunks.dto;

import com.cluely.meeting.entity.MeetingStatus;

public class ChunkProgressResponseDTO {

    private long totalChunks;
    private int lastSequenceNumber;
    private MeetingStatus meetingStatus;

    public ChunkProgressResponseDTO() {
    }

    public ChunkProgressResponseDTO(long totalChunks, int lastSequenceNumber, MeetingStatus meetingStatus) {
        this.totalChunks = totalChunks;
        this.lastSequenceNumber = lastSequenceNumber;
        this.meetingStatus = meetingStatus;
    }

    public long getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(long totalChunks) {
        this.totalChunks = totalChunks;
    }

    public int getLastSequenceNumber() {
        return lastSequenceNumber;
    }

    public void setLastSequenceNumber(int lastSequenceNumber) {
        this.lastSequenceNumber = lastSequenceNumber;
    }

    public MeetingStatus getMeetingStatus() {
        return meetingStatus;
    }

    public void setMeetingStatus(MeetingStatus meetingStatus) {
        this.meetingStatus = meetingStatus;
    }
}