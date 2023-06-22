package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.CourtConfiguration;

public class EndStrategy extends SUDRFStrategy {
    public EndStrategy(CourtConfiguration cc) {
        super(cc);
    }

    @Override
    public void run() {
        finish();
    }

    @Override
    protected void finish() {
    }
}
