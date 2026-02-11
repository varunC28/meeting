package com.cluely.audio_chunks.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import com.cluely.audio_chunks.exception.StorageException;

@Service
public class FileStorageService {

    private final Path storageLocation;
    private final long maxChunkSize;

    @Autowired
    public FileStorageService(
            Path audioChunksStorageLocation,
            @Value("${cluely.storage.max-chunk-size}") long maxChunkSize) {
        this.storageLocation = audioChunksStorageLocation;
        this.maxChunkSize = maxChunkSize;
    }

    public String storeChunk(UUID meetingId, Integer sequenceNumber, MultipartFile file) {
        validateChunk(file);

        String filename = generateFilename(meetingId, sequenceNumber, file.getOriginalFilename());
        Path destinationPath = this.storageLocation.resolve(filename);

        try {
            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
            return destinationPath.toString();
        } catch (IOException e) {
            throw new StorageException("Failed to store chunk: " + filename, e);
        }
    }

    private void validateChunk(MultipartFile file) {
        if (file.isEmpty()) {
            throw new StorageException("Cannot store empty file");
        }

        if (file.getSize() > maxChunkSize) {
            throw new StorageException("Chunk size exceeds maximum: " + maxChunkSize + " bytes");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
            throw new StorageException("Invalid content type. Expected audio/*, got: " + contentType);
        }
    }

    private String generateFilename(UUID meetingId, Integer sequenceNumber, String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return String.format("meeting_%s_chunk_%04d%s", meetingId.toString(), sequenceNumber, extension);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".webm";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}