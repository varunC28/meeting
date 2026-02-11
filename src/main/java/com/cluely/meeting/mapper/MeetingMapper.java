package com.cluely.meeting.mapper;

import com.cluely.meeting.dto.MeetingCreateRequestDto;
import com.cluely.meeting.dto.MeetingResponseDto;
import com.cluely.meeting.entity.MeetingEntity;

import java.time.LocalDateTime;

public class MeetingMapper {

    private MeetingMapper() {
        // prevent instantiation
    }

    public static MeetingEntity toEntity(MeetingCreateRequestDto dto) {
        MeetingEntity entity = new MeetingEntity();

        entity.setUserId(dto.getUserId());
        entity.setTitle(dto.getTitle());
        entity.setSource(dto.getSource());

        // backend-owned fields
        entity.setCreatedAt(LocalDateTime.now());

        return entity;
    }

    public static MeetingResponseDto toResponse(MeetingEntity entity) {
        MeetingResponseDto dto = new MeetingResponseDto();

        dto.setMeetingId(entity.getMeetingId());
        dto.setUserId(entity.getUserId());
        dto.setTitle(entity.getTitle());
        dto.setStartedAt(entity.getStartedAt());
        dto.setEndedAt(entity.getEndedAt());
        dto.setSource(entity.getSource());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setStatus(entity.getStatus());

        return dto;
    }

}
