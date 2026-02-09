package com.cluely.meeting.controller;

import com.cluely.meeting.dto.MeetingCreateRequestDto;
import com.cluely.meeting.dto.MeetingDashboardDto;
import com.cluely.meeting.dto.MeetingResponseDto;
import com.cluely.meeting.dto.MeetingUpdateRequestDto;
import com.cluely.meeting.entity.MeetingEntity;
import com.cluely.meeting.mapper.MeetingMapper;
import com.cluely.meeting.repository.MeetingRepository;
import com.cluely.meeting.service.MeetingService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.cluely.security.SecurityUtils.getCurrentUserId;

@RestController
@RequestMapping("/meetings")
@CrossOrigin(origins = "http://localhost:4200")
public class MeetingController {

    private final MeetingService service;
    private MeetingRepository repository;

    public MeetingController(MeetingService service) {
        this.service = service;
    }

    @GetMapping("/{meetingId}/dashboard")
    public MeetingDashboardDto dashboard(
            @PathVariable UUID meetingId,
            Pageable pageable) {

        return service.getDashboard(meetingId, pageable);
    }

    // Meeting Advance Filter endpoint
    @GetMapping("/filter")
    public Page<MeetingResponseDto> filter(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            Pageable pageable) {

        return service.getMeetingsFiltered(
                title, source, from, to, pageable);
    }

    @GetMapping
    public Page<MeetingResponseDto> list(Pageable pageable) {
        return service.getMyMeetings(pageable);
    }

    @GetMapping("/{meetingId}")
    public MeetingResponseDto get(@PathVariable UUID meetingId) {
        return service.getMyMeeting(meetingId);
    }

    @PutMapping("/{id}")
    public MeetingResponseDto update(
            @PathVariable UUID id,
            @Valid @RequestBody MeetingUpdateRequestDto dto) {
        return service.updateMeeting(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.deleteMeeting(id);
    }

    @PostMapping
    public MeetingResponseDto create(
            @Valid @RequestBody MeetingCreateRequestDto dto) {
        return service.createMeeting(dto);
    }

}