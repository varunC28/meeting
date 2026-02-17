// package com.cluely.audio_chunks.service;

// import com.cluely.audio_chunks.dto.AudioChunkResponseDTO;
// import com.cluely.audio_chunks.entity.AudioChunkEntity;
// import com.cluely.audio_chunks.entity.ChunkStatus;
// import com.cluely.audio_chunks.exception.DuplicateChunkException;
// import com.cluely.audio_chunks.exception.InvalidMeetingStateException;
// import com.cluely.audio_chunks.mapper.AudioChunkMapper;
// import com.cluely.audio_chunks.repository.AudioChunkRepository;
// import com.cluely.global.NotFoundException;
// import com.cluely.meeting.entity.MeetingEntity;
// import com.cluely.meeting.entity.MeetingStatus;
// import com.cluely.meeting.repository.MeetingRepository;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.web.multipart.MultipartFile;

// import java.time.LocalDateTime;
// import java.util.Optional;
// import java.util.UUID;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class AudioChunkServiceTest {

// @Mock
// private AudioChunkRepository chunkRepository;

// @Mock
// private MeetingRepository meetingRepository;

// @Mock
// private FileStorageService fileStorageService;

// @Mock
// private AudioChunkMapper mapper;

// @InjectMocks
// private AudioChunkService audioChunkService;

// private UUID meetingId;
// private UUID userId;
// private MeetingEntity meeting;
// private MultipartFile mockFile;

// @BeforeEach
// void setUp() {
// meetingId = UUID.randomUUID();
// userId = UUID.randomUUID();

// meeting = new MeetingEntity();
// meeting.setMeetingId(meetingId);
// meeting.setUserId(userId);
// meeting.setStatus(MeetingStatus.LIVE);
// meeting.setTitle("Test Meeting");

// mockFile = mock(MultipartFile.class);
// when(mockFile.getSize()).thenReturn(1000L);
// when(mockFile.getContentType()).thenReturn("audio/webm");
// when(mockFile.getOriginalFilename()).thenReturn("chunk.webm");
// }

// @Test
// void uploadChunk_Success() {
// // Arrange
// Integer sequenceNumber = 1;
// String filePath = "/path/to/chunk.webm";

// when(meetingRepository.findByMeetingIdAndUserIdAndDeletedFalse(meetingId,
// userId))
// .thenReturn(Optional.of(meeting));
// when(chunkRepository.existsByMeetingIdAndSequenceNumber(meetingId,
// sequenceNumber))
// .thenReturn(false);
// when(fileStorageService.storeChunk(meetingId, sequenceNumber, mockFile))
// .thenReturn(filePath);

// AudioChunkEntity savedChunk = new AudioChunkEntity();
// savedChunk.setChunkId(UUID.randomUUID());
// savedChunk.setSequenceNumber(sequenceNumber);

// when(chunkRepository.save(any(AudioChunkEntity.class))).thenReturn(savedChunk);

// AudioChunkResponseDTO responseDTO = new AudioChunkResponseDTO();
// when(mapper.toResponseDTO(savedChunk)).thenReturn(responseDTO);

// // Act
// AudioChunkResponseDTO result = audioChunkService.uploadChunk(meetingId,
// sequenceNumber, mockFile, userId);

// // Assert
// assertNotNull(result);
// verify(meetingRepository).findByMeetingIdAndUserIdAndDeletedFalse(meetingId,
// userId);
// verify(fileStorageService).storeChunk(meetingId, sequenceNumber, mockFile);
// verify(chunkRepository).save(any(AudioChunkEntity.class));
// }

// @Test
// void uploadChunk_MeetingNotFound_ThrowsException() {
// // Arrange
// when(meetingRepository.findByMeetingIdAndUserIdAndDeletedFalse(meetingId,
// userId))
// .thenReturn(Optional.empty());

// // Act & Assert
// assertThrows(NotFoundException.class, () ->
// audioChunkService.uploadChunk(meetingId, 1, mockFile, userId)
// );

// verify(chunkRepository, never()).save(any());
// }

// @Test
// void uploadChunk_MeetingNotLive_ThrowsException() {
// // Arrange
// meeting.setStatus(MeetingStatus.SCHEDULED);
// when(meetingRepository.findByMeetingIdAndUserIdAndDeletedFalse(meetingId,
// userId))
// .thenReturn(Optional.of(meeting));

// // Act & Assert
// assertThrows(InvalidMeetingStateException.class, () ->
// audioChunkService.uploadChunk(meetingId, 1, mockFile, userId)
// );

// verify(fileStorageService, never()).storeChunk(any(), any(), any());
// }

// @Test
// void uploadChunk_DuplicateSequence_ThrowsException() {
// // Arrange
// Integer sequenceNumber = 1;
// when(meetingRepository.findByMeetingIdAndUserIdAndDeletedFalse(meetingId,
// userId))
// .thenReturn(Optional.of(meeting));
// when(chunkRepository.existsByMeetingIdAndSequenceNumber(meetingId,
// sequenceNumber))
// .thenReturn(true);

// // Act & Assert
// assertThrows(DuplicateChunkException.class, () ->
// audioChunkService.uploadChunk(meetingId, sequenceNumber, mockFile, userId)
// );

// verify(fileStorageService, never()).storeChunk(any(), any(), any());
// }

// @Test
// void uploadChunk_WrongUser_ThrowsException() {
// // Arrange
// UUID differentUserId = UUID.randomUUID();
// when(meetingRepository.findByMeetingIdAndUserIdAndDeletedFalse(meetingId,
// differentUserId))
// .thenReturn(Optional.empty());

// // Act & Assert
// assertThrows(NotFoundException.class, () ->
// audioChunkService.uploadChunk(meetingId, 1, mockFile, differentUserId)
// );
// }
// }