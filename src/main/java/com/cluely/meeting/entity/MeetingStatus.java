package com.cluely.meeting.entity;

public enum MeetingStatus {
    SCHEDULED, // Meeting created but not started
    LIVE, // Currently recording audio
    PROCESSING, // Meeting audio is being processed
    COMPLETED, // Meeting ended
    FAILED, // Meeting failed to process
    CANCELLED // Meeting was cancelled
}