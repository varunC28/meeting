// package com.cluely.audio_chunks.controller;

// import com.cluely.audio_chunks.entity.AudioChunkEntity;
// import com.cluely.audio_chunks.repository.AudioChunkRepository;
// import com.cluely.meeting.entity.MeetingEntity;
// import com.cluely.meeting.entity.MeetingStatus;
// import com.cluely.meeting.repository.MeetingRepository;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import
// org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.MediaType;
// import org.springframework.mock.web.MockMultipartFile;
// import org.springframework.security.test.context.support.WithMockUser;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.UUID;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static
// org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static
// org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @SpringBootTest
// @AutoConfigureMockMvc
// @ActiveProfiles("test")
// @Transactional
// class AudioChunkControllerIntegrationTest {

// @Autowired
// private MockMvc mockMvc;

// @Autowired
// private MeetingRepository meetingRepository;

// @Autowired
// private AudioChunkRepository chunkRepository;

// private UUID userId;
// private MeetingEntity liveMeeting;

// @BeforeEach
// void setUp() {
// userId = UUID.randomUUID();

// // Create a LIVE meeting
// liveMeeting = new MeetingEntity();
// liveMeeting.setUserId(userId);
// liveMeeting.setTitle("Test Meeting");
// liveMeeting.setSource("web");
// liveMeeting.setStatus(MeetingStatus.LIVE);
// liveMeeting.setStartedAt(LocalDateTime.now());
// liveMeeting.setCreatedAt(LocalDateTime.now());
// liveMeeting.setDeleted(false);
// liveMeeting = meetingRepository.save(liveMeeting);
// }

// @Test
// @WithMockUser
// void uploadChunk_Success() throws Exception {
// // Arrange
// MockMultipartFile file = new MockMultipartFile(
// "audioFile",
// "chunk.webm",
// "audio/webm",
// "test audio data".getBytes()
// );

// // Act & Assert
// mockMvc.perform(multipart("/api/meetings/{meetingId}/chunks",
// liveMeeting.getMeetingId())
// .file(file)
// .param("sequenceNumber", "1"))
// .andExpect(status().isCreated())
// .andExpect(jsonPath("$.sequenceNumber").value(1))
// .andExpect(jsonPath("$.meetingId").value(liveMeeting.getMeetingId().toString()))
// .andExpect(jsonPath("$.status").value("UPLOADED"));

// // Verify chunk was saved in DB
// List<AudioChunkEntity> chunks =
// chunkRepository.findByMeetingIdOrderBySequenceNumberAsc(liveMeeting.getMeetingId());
// assertEquals(1, chunks.size());
// assertEquals(1, chunks.get(0).getSequenceNumber());
// }

// @Test
// @WithMockUser
// void uploadChunk_DuplicateSequence_ReturnsConflict() throws Exception {
// // Arrange - upload chunk 1 first
// MockMultipartFile file1 = new MockMultipartFile(
// "audioFile", "chunk.webm", "audio/webm", "data1".getBytes()
// );
// mockMvc.perform(multipart("/api/meetings/{meetingId}/chunks",
// liveMeeting.getMeetingId())
// .file(file1)
// .param("sequenceNumber", "1"))
// .andExpect(status().isCreated());

// // Act - try to upload chunk 1 again
// MockMultipartFile file2 = new MockMultipartFile(
// "audioFile", "chunk.webm", "audio/webm", "data2".getBytes()
// );

// mockMvc.perform(multipart("/api/meetings/{meetingId}/chunks",
// liveMeeting.getMeetingId())
// .file(file2)
// .param("sequenceNumber", "1"))
// .andExpect(status().isConflict())
// .andExpect(jsonPath("$.error").value("DUPLICATE_CHUNK"));
// }

// @Test
// @WithMockUser
// void uploadChunk_MeetingNotLive_ReturnsBadRequest() throws Exception {
// // Arrange - change meeting to SCHEDULED
// liveMeeting.setStatus(MeetingStatus.SCHEDULED);
// meetingRepository.save(liveMeeting);

// MockMultipartFile file = new MockMultipartFile(
// "audioFile", "chunk.webm", "audio/webm", "data".getBytes()
// );

// // Act & Assert
// mockMvc.perform(multipart("/api/meetings/{meetingId}/chunks",
// liveMeeting.getMeetingId())
// .file(file)
// .param("sequenceNumber", "1"))
// .andExpect(status().isBadRequest())
// .andExpect(jsonPath("$.error").value("INVALID_MEETING_STATE"));
// }

// @Test
// @WithMockUser
// void getChunks_ReturnsChunksInOrder() throws Exception {
// // Arrange - upload 3 chunks
// for (int i = 1; i <= 3; i++) {
// MockMultipartFile file = new MockMultipartFile(
// "audioFile", "chunk.webm", "audio/webm", ("data" + i).getBytes()
// );
// mockMvc.perform(multipart("/api/meetings/{meetingId}/chunks",
// liveMeeting.getMeetingId())
// .file(file)
// .param("sequenceNumber", String.valueOf(i)))
// .andExpect(status().isCreated());
// }

// // Act & Assert
// mockMvc.perform(get("/api/meetings/{meetingId}/chunks",
// liveMeeting.getMeetingId()))
// .andExpect(status().isOk())
// .andExpect(jsonPath("$.length()").value(3))
// .andExpect(jsonPath("$[0].sequenceNumber").value(1))
// .andExpect(jsonPath("$[1].sequenceNumber").value(2))
// .andExpect(jsonPath("$[2].sequenceNumber").value(3));
// }

// @Test
// @WithMockUser
// void getProgress_ReturnsCorrectCount() throws Exception {
// // Arrange - upload 5 chunks
// for (int i = 1; i <= 5; i++) {
// MockMultipartFile file = new MockMultipartFile(
// "audioFile", "chunk.webm", "audio/webm", ("data" + i).getBytes()
// );
// mockMvc.perform(multipart("/api/meetings/{meetingId}/chunks",
// liveMeeting.getMeetingId())
// .file(file)
// .param("sequenceNumber", String.valueOf(i)))
// .andExpect(status().isCreated());
// }

// // Act & Assert
// mockMvc.perform(get("/api/meetings/{meetingId}/chunks/progress",
// liveMeeting.getMeetingId()))
// .andExpect(status().isOk())
// .andExpect(jsonPath("$.totalChunks").value(5))
// .andExpect(jsonPath("$.lastSequenceNumber").value(5))
// .andExpect(jsonPath("$.meetingStatus").value("LIVE"));
// }

// @Test
// @WithMockUser
// void uploadChunk_MeetingNotFound_ReturnsNotFound() throws Exception {
// // Arrange
// UUID nonExistentMeetingId = UUID.randomUUID();
// MockMultipartFile file = new MockMultipartFile(
// "audioFile", "chunk.webm", "audio/webm", "data".getBytes()
// );

// // Act & Assert
// mockMvc.perform(multipart("/api/meetings/{meetingId}/chunks",
// nonExistentMeetingId)
// .file(file)
// .param("sequenceNumber", "1"))
// .andExpect(status().isNotFound());
// }
// }