package com.cluely.audio_chunks.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateChunkException extends RuntimeException {
    public DuplicateChunkException(String message) {
        super(message);
    }
}