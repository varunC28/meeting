package com.cluely.note.repository;

import com.cluely.note.entity.NoteEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NoteRepository
        extends JpaRepository<NoteEntity, UUID> {

    Page<NoteEntity> findByUserId(UUID userId, Pageable pageable);

    Page<NoteEntity> findByMeetingId(UUID meetingId, Pageable pageable);

    List<NoteEntity> findByMeetingIdOrderByCreatedAtDesc(UUID meetingId);

    Page<NoteEntity> findByMeetingIdAndDeletedFalse(UUID meetingId, Pageable pageable);

}
