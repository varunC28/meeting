package com.cluely.ai.speech.service;

import com.cluely.ai.speech.dto.TranscriptionRequestDTO;
import com.cluely.ai.speech.dto.TranscriptionResponseDTO;

public interface SpeechToTextService {

    // Transcribe a single audio chunk (real-time pipeline)
    TranscriptionResponseDTO transcribeChunk(TranscriptionRequestDTO request);

    // Transcribe full meeting audio (post-meeting pipeline)
    TranscriptionResponseDTO transcribeFullAudio(TranscriptionRequestDTO request);
}
