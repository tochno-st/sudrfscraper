package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.configuration.ApplicationConfiguration;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.CourtConfiguration;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.Issue;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.SearchPattern;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.StrategyName;
import com.github.courtandrey.sudrfscraper.dump.model.Case;
import com.github.courtandrey.sudrfscraper.service.CasesPipeLineFactory;
import com.github.courtandrey.sudrfscraper.service.Constant;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.service.logger.LoggingLevel;
import com.github.courtandrey.sudrfscraper.service.SeleniumHelper;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;
import com.github.courtandrey.sudrfscraper.service.ThreadHelper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Set;

public abstract class ConnectionSUDRFStrategy extends SUDRFStrategy {
    boolean isInTestingMode = false;

    protected Parser parser;

    public ConnectionSUDRFStrategy(CourtConfiguration cc) {
        super(cc);
        if (cc.getStrategyName() != StrategyName.MOSGORSUD_STRATEGY) {
            parser = new GeneralParser(cc);
        } else {
            parser = new MosGorSudParser(cc);
        }
        if (ApplicationConfiguration.getInstance().getProperty("dev.test") != null
                && ApplicationConfiguration.getInstance().getProperty("dev.test").equals("true")) {
            isInTestingMode = true;
        }
    }

    @Override
    public void run() {
        do {
            String message = String.format(Message.EXECUTION_STATUS_BEGINNING.toString(),cc.getName(),urls[indexUrl]);
            doCircle();
            SimpleLogger.log(LoggingLevel.INFO,message + " " + String.format(Message.EXECUTION_STATUS_MID.toString(),issue));
        } while (!timeToStopRotatingSrv);
    }
    private void doCircle() {
        if (isInTestingMode && finalIssue == Issue.SUCCESS) {
            timeToStopRotatingSrv = true;
            return;
        }
        connect();
        if (checkPreventable()) {
            timeToStopRotatingSrv = true;
            return;
        }

        String text = currentDocument.text();

        prevSize = resultCases.size();
        checkText(text);
        if (issue == Issue.SUCCESS) {
            getCases();
            if (checkConditions()) return;
            rotate();
        }

        else if (Issue.isPreventable(issue)) {
            if (checkPreventable()) {
                timeToStopRotatingSrv = true;
                return;
            }
            refresh();
        }

        else if (issue == Issue.NOT_FOUND_CASE) {
            if (checkConditions()) return;
            rotate();
        }

        else {
            timeToStopRotatingSrv = true;
        }
    }

    private void refresh() {
        if (cc.getConnection() == Connection.SELENIUM) {
            SeleniumHelper.getInstance().refresh();
        }
        unravel = unravel - 5;
    }

    protected void connect() {
        if (cc.getConnection() == Connection.REQUEST) {
            connectJsoup();
        } else if (cc.getConnection() == Connection.SELENIUM) {
            connectSelenium();
        }
    }

    private void connectSelenium() {
        try {
            int waitTime = 14 + (10-unravel);
            if (page_num > 1 && (cc.getSearchPattern() == SearchPattern.SECONDARY_PATTERN ||
                    cc.getSearchPattern() == SearchPattern.DEPRECATED_SECONDARY_PATTERN)) {
                waitTime = 5;
            }
            SeleniumHelper sh = SeleniumHelper.getInstance();
            String page = sh.getPage(urls[indexUrl], waitTime);

            currentDocument = Jsoup.parse(page);
        }
        catch (WebDriverException e) {
            if (unravel > 0) {
                ThreadHelper.sleep(5);
                unravel = unravel - 2;
                connectSelenium();
            }
            else {
                if (e instanceof TimeoutException) {
                    finalIssue = Issue.compareAndSetIssue(Issue.CONNECTION_ERROR, finalIssue);
                    issue = Issue.CONNECTION_ERROR;
                } else {
                    finalIssue = Issue.compareAndSetIssue(Issue.URL_ERROR, finalIssue);
                    issue = Issue.URL_ERROR;
                }
            }
        }
    }

    private void connectJsoup() {
        try {
            try(CloseableHttpClient httpClient =  HttpClients.custom().disableContentCompression().setRedirectStrategy(new LaxRedirectStrategy()).build()) {
                HttpGet get = new HttpGet(urls[indexUrl]);
                get.setHeader("User-Agent", Constant.UA.toString());
                HttpResponse response = httpClient.execute(get);
                String htmlString = EntityUtils.toString(response.getEntity());
                currentDocument = Jsoup.parse(htmlString);
            }
        } catch (SocketException | HttpStatusException | UnknownHostException e) {
            if (unravel > 0) {
                ThreadHelper.sleep(5);
                --unravel;
                connectJsoup();
            } else {
                finalIssue = Issue.compareAndSetIssue(Issue.CONNECTION_ERROR, finalIssue);
                issue = Issue.CONNECTION_ERROR;
            }
        } catch (SocketTimeoutException e) {
            if (unravel>0) {
                ThreadHelper.sleep(5);
                unravel = unravel - 2;
                connectJsoup();
            } else {
                finalIssue = Issue.compareAndSetIssue(Issue.URL_ERROR, finalIssue);
                issue = Issue.URL_ERROR;
            }
        } catch (IOException e) {
            unravel = 0;
            finalIssue = Issue.compareAndSetIssue(Issue.CONNECTION_ERROR, finalIssue);
            issue = Issue.CONNECTION_ERROR;
        }
    }

    @Override
    protected void finish() {
        if (issue == Issue.SUCCESS)
            resultCases = filterCases();
        if (resultCases.size() > 150_000) {
            parser.scrapTextsAndFlush(resultCases, CasesPipeLineFactory.getInstance().getPipeLine());
            resultCases.clear();
        }
        resultCases = parser.scrapTexts(resultCases);
        super.finish();
    }

    @Override
    protected void logFinalInfo() {
        if (!parser.isTextFound() && resultCases.size() != 0 && resultCases.size() >= 25) {
            SimpleLogger.log(LoggingLevel.DEBUG, Message.NO_TEXT_FOUND + urls[indexUrl]);
        }
        if ((resultCases.size() % 25 == 0 || resultCases.size() % 20 == 0) && resultCases.size() != 0 && !isInTestingMode) {
            SimpleLogger.log(LoggingLevel.DEBUG, Message.SUSPICIOUS_NUMBER_OF_CASES + urls[indexUrl]);
        }
        super.logFinalInfo();
    }

    private boolean checkPreventable() {
        return unravel <= 0;
    }

    private void getCases() {
        if (cc.getVnkod() == null) setVnkod(currentDocument);
        Set<Case> cases = parser.scrap(currentDocument,cc.getSearchString());

        if (cases != null) {
            resultCases.addAll(cases);
        }

        if (resultCases != null && resultCases.size() > 0) {
            finalIssue = Issue.compareAndSetIssue(Issue.SUCCESS,finalIssue);
            issue = Issue.SUCCESS;
        } else {
            finalIssue = Issue.compareAndSetIssue(Issue.NOT_FOUND_CASE,finalIssue);
            issue = Issue.NOT_FOUND_CASE;
        }
    }

}