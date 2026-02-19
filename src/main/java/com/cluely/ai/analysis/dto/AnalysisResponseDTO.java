package com.cluely.ai.analysis.dto;

import java.util.List;

public class AnalysisResponseDTO {

    private String summary;

    private List<String> actionItems;

    private List<String> decisions;

    private List<String> keyTopics;

    private String sentiment; // POSITIVE, NEUTRAL, NEGATIVE

    public AnalysisResponseDTO() {
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getActionItems() {
        return actionItems;
    }

    public void setActionItems(List<String> actionItems) {
        this.actionItems = actionItems;
    }

    public List<String> getDecisions() {
        return decisions;
    }

    public void setDecisions(List<String> decisions) {
        this.decisions = decisions;
    }

    public List<String> getKeyTopics() {
        return keyTopics;
    }

    public void setKeyTopics(List<String> keyTopics) {
        this.keyTopics = keyTopics;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }
}