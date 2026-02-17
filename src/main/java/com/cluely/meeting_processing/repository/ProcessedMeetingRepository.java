package com.cluely.meeting_processing.repository;

import com.cluely.meeting_processing.entity.ProcessedMeetingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProcessedMeetingRepository extends JpaRepository<ProcessedMeetingEntity, UUID> {

    Optional<ProcessedMeetingEntity> findByMeetingId(UUID meetingId);

    boolean existsByMeetingId(UUID meetingId);
}