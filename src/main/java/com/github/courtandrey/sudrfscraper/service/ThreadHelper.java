package com.github.courtandrey.sudrfscraper.service;

import com.github.courtandrey.sudrfscraper.service.logger.LoggingLevel;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ThreadHelper {
    public void sleep(double seconds) {
        try {
            Thread.sleep((long)(1000 * seconds));
        } catch (InterruptedException e) {
            SimpleLogger.log(LoggingLevel.ERROR, String.format(Message.EXCEPTION_OCCURRED.toString(), e));
        }
    }
}
