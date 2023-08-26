package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.CourtConfiguration;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Instance;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.SearchRequest;

public class MosGorSudStrategy extends ConnectionSUDRFStrategy{
    public MosGorSudStrategy(CourtConfiguration cc) {
        super(cc);
    }

    @Override
    public void run() {
        for (Instance i: SearchRequest.getInstance().getInstanceList()) {
            currentInstance = i;
            if (!checkArticleAndInstance(i)) continue;
            createUrls(i);
            super.run();
            clear();
            issue = null;
            timeToStopRotatingPage = false;
        }
        finish();
    }


}
