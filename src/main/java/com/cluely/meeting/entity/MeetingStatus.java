package com.cluely.meeting.entity;

public enum MeetingStatus {
    SCHEDULED, // Meeting created but not started
    LIVE, // Currently recording audio
    COMPLETED, // Meeting ended
    CANCELLED // Meeting was cancelled
}