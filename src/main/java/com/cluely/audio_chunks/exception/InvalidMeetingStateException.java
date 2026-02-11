package com.cluely.audio_chunks.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidMeetingStateException extends RuntimeException {
    public InvalidMeetingStateException(String message) {
        super(message);
    }
}