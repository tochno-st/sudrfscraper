package com.github.courtandrey.sudrfscraper.web.dto;

public class ProgressData {
    private int current;
    private int total;
    private int scraped;

    public int getScraped() {
        return scraped;
    }

    public ProgressData(int current, int total, int scraped) {
        this.current = current;
        this.total = total;
        this.scraped = scraped;
    }

    public int getCurrent() {
        return current;
    }

    public int getTotal() {
        return total;
    }
}
