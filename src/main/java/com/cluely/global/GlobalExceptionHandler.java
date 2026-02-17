package com.cluely.global;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.cluely.audio_chunks.exception.DuplicateChunkException;
import com.cluely.audio_chunks.exception.InvalidMeetingStateException;
import com.cluely.audio_chunks.exception.StorageException;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));

        Map<String, Object> response = new HashMap<>();
        response.put("error", "VALIDATION_ERROR");
        response.put("fields", fieldErrors);

        return response;
    }

    // ✅ Fallback — any uncaught error
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGeneric(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "SERVER_ERROR");
        response.put("message", ex.getMessage());
        return response;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDataIntegrity(
            DataIntegrityViolationException ex) {

        return Map.of(
                "error", "Data integrity violation",
                "message", "Duplicate or invalid reference detected");
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(NotFoundException ex) {
        return Map.of(
                "error", "Not found",
                "message", ex.getMessage());
    }

    @ExceptionHandler(StorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleStorageException(StorageException ex) {
        return Map.of(
                "error", "STORAGE_ERROR",
                "message", ex.getMessage());
    }

    @ExceptionHandler(DuplicateChunkException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDuplicateChunk(DuplicateChunkException ex) {
        return Map.of(
                "error", "DUPLICATE_CHUNK",
                "message", ex.getMessage());
    }

    @ExceptionHandler(InvalidMeetingStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidMeetingState(InvalidMeetingStateException ex) {
        return Map.of(
                "error", "INVALID_MEETING_STATE",
                "message", ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public Map<String, String> handleFileSizeLimitExceeded(MaxUploadSizeExceededException ex) {
        return Map.of(
                "error", "FILE_TOO_LARGE",
                "message", "Chunk exceeds maximum size limit");
    }
}
