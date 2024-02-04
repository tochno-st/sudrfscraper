package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Instance;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.SearchRequest;
import com.github.courtandrey.sudrfscraper.exception.CaptchaException;
import com.github.courtandrey.sudrfscraper.service.CaptchaPropertiesConfigurator;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.CourtConfiguration;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.Issue;
import com.github.courtandrey.sudrfscraper.service.logger.LoggingLevel;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;
import org.openqa.selenium.TimeoutException;

import static com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.Issue.*;

public class CaptchaStrategy extends ConnectionSUDRFStrategy {
    private boolean didWellItWorkedOnceUsed = false;
    public CaptchaStrategy(CourtConfiguration cc) {
        super(cc);
    }

    int captchaInLoop = 0;

    int prevNum = 1;
    int prevSrvNum = 1;

    @Override
    public void run() {
        for (Instance i: SearchRequest.getInstance().getInstanceList()) {
            currentInstance = i;
            if (!checkArticleAndInstance(i)) continue;
            upperRun(i);
            clear();
            issue = null;
            timeToStopRotatingPage = false;
            indexUrl = 0;
        }
        finish();
    }

    private void upperRun(Instance i) {
        try {
            createUrls(i);
            for (indexUrl = 0; indexUrl < urls.length; indexUrl++) {
                super.run();
                if (issue == Issue.CAPTCHA && !(finalIssue == NOT_FOUND_CASE && indexUrl > 0)) {
                    if (captchaInLoop == 5 && page_num == prevNum && srv_num == prevSrvNum) {
                        if (indexUrl + 1 == urls.length) {
                            finalIssue = Issue.compareAndSetIssue(LOOPED_CAPTCHA,finalIssue);
                            break;
                        }
                        else {
                            captchaInLoop = 0;
                            continue;
                        }
                    } else if (captchaInLoop == 5){
                        captchaInLoop = 0;
                    }

                    prevNum = page_num;
                    prevSrvNum = srv_num;
                    captchaInLoop += 1;
                    issue = null;
                    timeToStopRotatingSrv = false;

                    CaptchaPropertiesConfigurator.configureCaptcha(cc, didWellItWorkedOnceUsed,cc.getConnection(),i);

                    didWellItWorkedOnceUsed = true;

                    String url = urls[indexUrl];

                    createUrls(i);
                    refreshUrls();

                    if (!url.equals(urls[indexUrl])) {
                        indexUrl = indexUrl - 1;
                    } else {
                        clear();
                        captchaInLoop = 0;
                    }
                }
                else if (finalIssue == SUCCESS) {
                    indexUrl += 1;
                    break;
                }
                else {
                    captchaInLoop = 0;
                    clear();
                }
            }
            indexUrl -= 1;
        }

        catch (InterruptedException e) {
            SimpleLogger.log(LoggingLevel.ERROR, String.format(Message.EXCEPTION_OCCURRED.toString(),e));
            finalIssue = Issue.ERROR;
        }
        catch (TimeoutException e) {
            finalIssue = Issue.CONNECTION_ERROR;
        }
        catch (CaptchaException e) {
            finalIssue = Issue.CAPTCHA_NOT_CONFIGURABLE;
        }
    }

    private void refreshUrls() {
        for (int i = 0; i< urls.length; i++) {
            urls[i] = urls[i].replace("page="+1,"page="+page_num);
            urls[i] = urls[i].replace("srv_num="+1,"srv_num="+srv_num);
            urls[i] = urls[i].replace("num_build="+1,"num_build="+(build));
        }
    }

}
