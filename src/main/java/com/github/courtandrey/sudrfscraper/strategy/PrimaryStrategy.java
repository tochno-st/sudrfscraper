package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.CourtConfiguration;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.Issue;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Instance;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.SearchRequest;


public class PrimaryStrategy extends ConnectionSUDRFStrategy {

    public PrimaryStrategy(CourtConfiguration cc) {
        super(cc);
    }

    @Override
    public void run() {
        for (Instance i: SearchRequest.getInstance().getInstanceList()) {
            currentInstance = i;
            if (!checkArticleAndInstance(i)) continue;
            createUrls(i);
            iterateThroughUrls();
            clear();
            issue = null;
            timeToStopRotatingPage = false;
            indexUrl = 0;
        }
        finish();
    }

    private void iterateThroughUrls() {
        for (; indexUrl < urls.length; indexUrl++) {
            super.run();
            if (finalIssue == Issue.SUCCESS) {
                indexUrl++;
                break;
            } else {
                clear();
            }
        }
        --indexUrl;
    }

}
