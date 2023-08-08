package com.github.courtandrey.sudrfscraper.web.dto;

import lombok.Getter;

@Getter
public class ProgressInfo {
    public ProgressInfo(String text, InfoType type) {
        this.text = text;
        this.type = type;
    }

    private final String text;
    private final InfoType type;
}
