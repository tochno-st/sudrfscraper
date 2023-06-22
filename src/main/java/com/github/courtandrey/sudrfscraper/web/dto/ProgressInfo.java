package com.github.courtandrey.sudrfscraper.web.dto;

public class ProgressInfo {
    public ProgressInfo(String text, InfoType type) {
        this.text = text;
        this.type = type;
    }

    private final String text;
    private final InfoType type;

    public String getText() {
        return text;
    }

    public InfoType getType() {
        return type;
    }
}
