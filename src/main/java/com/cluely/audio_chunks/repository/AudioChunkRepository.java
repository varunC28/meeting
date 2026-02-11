package com.cluely.audio_chunks.repository;

import com.cluely.audio_chunks.entity.AudioChunkEntity;
import com.cluely.audio_chunks.entity.ChunkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AudioChunkRepository extends JpaRepository<AudioChunkEntity, UUID> {

    @Query("SELECT ac FROM AudioChunkEntity ac WHERE ac.meeting.meetingId = :meetingId AND ac.deleted = false ORDER BY ac.sequenceNumber ASC")
    List<AudioChunkEntity> findByMeetingIdOrderBySequenceNumberAsc(@Param("meetingId") UUID meetingId);

    @Query("SELECT ac FROM AudioChunkEntity ac WHERE ac.meeting.meetingId = :meetingId AND ac.sequenceNumber = :sequenceNumber AND ac.deleted = false")
    Optional<AudioChunkEntity> findByMeetingIdAndSequenceNumber(@Param("meetingId") UUID meetingId,
            @Param("sequenceNumber") Integer sequenceNumber);

    @Query("SELECT MAX(ac.sequenceNumber) FROM AudioChunkEntity ac WHERE ac.meeting.meetingId = :meetingId AND ac.deleted = false")
    Optional<Integer> findMaxSequenceNumber(@Param("meetingId") UUID meetingId);

    @Query("SELECT COUNT(ac) > 0 FROM AudioChunkEntity ac WHERE ac.meeting.meetingId = :meetingId AND ac.sequenceNumber = :sequenceNumber AND ac.deleted = false")
    boolean existsByMeetingIdAndSequenceNumber(@Param("meetingId") UUID meetingId,
            @Param("sequenceNumber") Integer sequenceNumber);

    @Query("SELECT COUNT(ac) FROM AudioChunkEntity ac WHERE ac.meeting.meetingId = :meetingId AND ac.status = :status AND ac.deleted = false")
    long countByMeetingIdAndStatus(@Param("meetingId") UUID meetingId, @Param("status") ChunkStatus status);
}