package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.CourtConfiguration;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.SearchPattern;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.Issue;


public class PrimaryStrategy extends ConnectionSUDRFStrategy {

    public PrimaryStrategy(CourtConfiguration cc) {
        super(cc);
    }

    @Override
    public void run() {
        createUrls();
        iterateThroughUrls();
        if (checkSuccessAndChangePattern(SearchPattern.SECONDARY_PATTERN,SearchPattern.DEPRECATED_SECONDARY_PATTERN) ||
                checkSuccessAndChangePattern(SearchPattern.DEPRECATED_SECONDARY_PATTERN,SearchPattern.SECONDARY_PATTERN)) {
            iterateThroughUrls();
        }
        finish();
    }

    private boolean checkSuccessAndChangePattern(SearchPattern src, SearchPattern trg) {
        if (finalIssue != Issue.SUCCESS && cc.getSearchPattern()==src) {
            cc.setSearchPattern(trg);
            indexUrl = 0;
            createUrls();
            return true;
        }
        return false;
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
