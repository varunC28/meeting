package com.cluely.ai.analysis.service;

import com.cluely.ai.analysis.dto.AnalysisRequestDTO;
import com.cluely.ai.analysis.dto.AnalysisResponseDTO;

public interface AIAnalysisService {

    /**
     * Analyze full meeting transcript
     * Returns summary, action items, decisions
     */
    AnalysisResponseDTO analyzeMeeting(AnalysisRequestDTO request);
}