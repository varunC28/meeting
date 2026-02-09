package com.cluely.meeting.repository;

import com.cluely.meeting.entity.MeetingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MeetingRepository extends JpaRepository<MeetingEntity, UUID>, JpaSpecificationExecutor<MeetingEntity> {

        Page<MeetingEntity> findByUserId(UUID userId, Pageable pageable);

        Page<MeetingEntity> findByTitleContainingIgnoreCase(
                        String title,
                        Pageable pageable);

        Page<MeetingEntity> findBySource(
                        String source,
                        Pageable pageable);

        Page<MeetingEntity> findByCreatedAtBetween(
                        LocalDateTime from,
                        LocalDateTime to,
                        Pageable pageable);

        Page<MeetingEntity> findByDeletedFalse(Pageable pageable);

        Page<MeetingEntity> findByUserIdAndDeletedFalse(UUID userId, Pageable pageable);

        Optional<MeetingEntity> findByMeetingIdAndUserIdAndDeletedFalse(
                        UUID meetingId,
                        UUID userId);

}
