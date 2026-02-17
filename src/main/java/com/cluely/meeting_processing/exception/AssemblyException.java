package com.cluely.meeting_processing.exception;

public class AssemblyException extends RuntimeException {
    public AssemblyException(String message) {
        super(message);
    }

    public AssemblyException(String message, Throwable cause) {
        super(message, cause);
    }
}