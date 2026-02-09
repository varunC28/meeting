package com.cluely.transcript.repository;

import com.cluely.transcript.entity.TranscriptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TranscriptRepository
        extends JpaRepository<TranscriptEntity, UUID> {

    Page<TranscriptEntity> findByMeetingId(UUID meetingId, Pageable pageable);

    Page<TranscriptEntity> findByMeetingIdAndDeletedFalse(UUID meetingId, Pageable pageable);

}
