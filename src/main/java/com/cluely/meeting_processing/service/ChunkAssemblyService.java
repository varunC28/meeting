package com.cluely.meeting_processing.service;

import com.cluely.ai.speech.service.TranscriptionService;
import com.cluely.audio_chunks.entity.AudioChunkEntity;
import com.cluely.meeting_processing.entity.ProcessedMeetingEntity;
import com.cluely.meeting_processing.entity.ProcessingStatus;
import com.cluely.meeting_processing.exception.AssemblyException;
import com.cluely.meeting_processing.repository.ProcessedMeetingRepository;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ChunkAssemblyService {

    private static final Logger log = LoggerFactory.getLogger(ChunkAssemblyService.class);

    private final ProcessedMeetingRepository processedMeetingRepository;
    private final TranscriptionService transcriptionService;
    private final FFmpeg ffmpeg;
    private final Path outputPath;

    public ChunkAssemblyService(
            ProcessedMeetingRepository processedMeetingRepository,
            TranscriptionService transcriptionService,
            @Value("${ffmpeg.path}") String ffmpegPath,
            @Value("${cluely.storage.output-path}") String outputPath) throws IOException {
        this.processedMeetingRepository = processedMeetingRepository;
        this.transcriptionService = transcriptionService;
        this.ffmpeg = new FFmpeg(ffmpegPath);
        this.outputPath = Paths.get(outputPath);
        Files.createDirectories(this.outputPath);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProcessedMeetingEntity assembleChunks(List<AudioChunkEntity> chunks) {
        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("Cannot assemble empty chunk list");
        }

        UUID meetingId = chunks.get(0).getMeeting().getMeetingId();
        log.info("Starting assembly for meeting: {}", meetingId);

        // Create and save initial record
        ProcessedMeetingEntity processed = new ProcessedMeetingEntity();
        processed.setMeetingId(meetingId);
        processed.setChunkCount(chunks.size());
        processed.setProcessingStatus(ProcessingStatus.IN_PROGRESS);
        log.info("Saving initial ProcessedMeeting record...");
        processed = processedMeetingRepository.save(processed);
        log.info("Saved with ID: {}", processed.getProcessedMeetingId());

        try {
            // Step 1: Create concat file
            Path concatFilePath = createConcatFile(chunks, meetingId);

            // Step 2: Build output file path
            String outputFileName = String.format("meeting_%s_full.mp3", meetingId.toString());
            Path outputFile = this.outputPath.resolve(outputFileName);

            // Step 3: FFmpeg concatenation
            concatenateAudioFiles(concatFilePath, outputFile);

            // Step 4: Get file info
            long fileSize = Files.size(outputFile);
            long duration = calculateTotalDuration(chunks);

            // Step 5: Update record
            processed.setFullAudioPath(outputFile.toString());
            processed.setFileSizeBytes(fileSize);
            processed.setTotalDurationMs(duration);
            processed.setProcessingStatus(ProcessingStatus.COMPLETED);
            processed.setProcessedAt(LocalDateTime.now());

            // Clean up concat file
            Files.deleteIfExists(concatFilePath);

            log.info("Successfully assembled meeting: {} -> {}", meetingId, outputFile);
            log.info("Saving COMPLETED status...");
            ProcessedMeetingEntity saved = processedMeetingRepository.save(processed);
            log.info("Final save successful: {}", saved.getProcessedMeetingId());

            // Step 6: Trigger full audio transcription (post-meeting pipeline)
            transcriptionService.transcribeFullAudioAsync(
                    saved.getMeetingId(),
                    saved.getFullAudioPath());
            log.info("Full audio transcription triggered for meeting: {}", meetingId);

            return saved;

        } catch (Exception e) {
            log.error("Assembly failed: {}", e.getMessage(), e);
            processed.setProcessingStatus(ProcessingStatus.FAILED);
            processed.setErrorMessage(e.getMessage());
            processedMeetingRepository.save(processed);
            throw new AssemblyException("Failed to assemble audio chunks", e);
        }
    }

    private Path createConcatFile(List<AudioChunkEntity> chunks, UUID meetingId) throws IOException {
        Path concatFilePath = this.outputPath.resolve("concat_" + meetingId + ".txt");

        try (BufferedWriter writer = Files.newBufferedWriter(concatFilePath)) {
            for (AudioChunkEntity chunk : chunks) {
                String normalizedPath = chunk.getFilePath().replace("\\", "/");
                writer.write(String.format("file '%s'\n", normalizedPath));
            }
        }

        log.debug("Created concat file: {} with {} chunks", concatFilePath, chunks.size());
        return concatFilePath;
    }

    private void concatenateAudioFiles(Path concatFile, Path outputFile) throws IOException {
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(concatFile.toString())
                .addExtraArgs("-f", "concat")
                .addExtraArgs("-safe", "0")
                .addOutput(outputFile.toString())
                .addExtraArgs("-c", "copy")
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);
        executor.createJob(builder).run();

        log.debug("FFmpeg concatenation completed: {}", outputFile);
    }

    private long calculateTotalDuration(List<AudioChunkEntity> chunks) {
        return chunks.stream()
                .filter(chunk -> chunk.getDurationMs() != null)
                .mapToLong(AudioChunkEntity::getDurationMs)
                .sum();
    }

    @Transactional(readOnly = true)
    public boolean isAlreadyProcessed(UUID meetingId) {
        return processedMeetingRepository.existsByMeetingId(meetingId);
    }

    @Transactional(readOnly = true)
    public ProcessedMeetingEntity getProcessedMeeting(UUID meetingId) {
        return processedMeetingRepository.findByMeetingId(meetingId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No processed meeting found for: " + meetingId));
    }
}