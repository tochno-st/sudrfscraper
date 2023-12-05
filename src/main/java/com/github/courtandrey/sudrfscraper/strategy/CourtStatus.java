package com.github.courtandrey.sudrfscraper.strategy;

public class CourtStatus {
    public final int page;
    public final Integer srv;
    public final Integer build;

    public CourtStatus(int page, Integer srv, Integer build) {
        this.page = page;
        this.srv = srv;
        this.build = build;
    }
}
