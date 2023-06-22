package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.CourtConfiguration;

public class MosGorSudStrategy extends ConnectionSUDRFStrategy{
    public MosGorSudStrategy(CourtConfiguration cc) {
        super(cc);
    }

    @Override
    public void run() {
        createUrls();
        super.run();
        finish();
    }


}
