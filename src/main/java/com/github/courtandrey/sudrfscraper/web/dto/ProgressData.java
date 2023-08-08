package com.github.courtandrey.sudrfscraper.web.dto;

import lombok.Getter;

@Getter
public class ProgressData {
    private final int current;
    private final int total;
    private final int scraped;

    public ProgressData(int current, int total, int scraped) {
        this.current = current;
        this.total = total;
        this.scraped = scraped;
    }
}
