package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.Issue;
import com.github.courtandrey.sudrfscraper.exception.CaptchaException;
import com.github.courtandrey.sudrfscraper.exception.VnkodNotFoundException;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.service.logger.LoggingLevel;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;

public class StrategyUEH {
    public void handle(Runnable t, Throwable e) {
        SUDRFStrategy strategy = (SUDRFStrategy) t;
        if (e instanceof VnkodNotFoundException) {
            strategy.finalIssue = Issue.compareAndSetIssue(Issue.CONFIGURATION_ERROR, strategy.finalIssue);
        }
        else if (e instanceof CaptchaException) {
            strategy.finalIssue = Issue.compareAndSetIssue(Issue.INACTIVE_COURT, strategy.finalIssue);
        }
        else {
            SimpleLogger.log(LoggingLevel.ERROR,String.format(Message.EXECUTION_EXCEPTION_OCCURRED.toString(), e.toString())
                    + strategy.cc.getSearchString());
            strategy.finalIssue = Issue.compareAndSetIssue(Issue.ERROR, strategy.finalIssue);
        }
        strategy.finish();
    }

}
