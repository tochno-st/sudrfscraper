package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.configuration.ApplicationConfiguration;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.*;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Instance;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.SearchRequest;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.article.SoftStrictFilterableArticle;
import com.github.courtandrey.sudrfscraper.dump.model.Case;
import com.github.courtandrey.sudrfscraper.service.ConfigurationHelper;
import com.github.courtandrey.sudrfscraper.service.SoftStrictFilterer;
import com.github.courtandrey.sudrfscraper.service.URLCreator;
import com.github.courtandrey.sudrfscraper.service.logger.LoggingLevel;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;
import lombok.Getter;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class SUDRFStrategy implements Runnable{
    protected int srv_num = 1;
    protected int page_num = 1;
    protected Document currentDocument = null;
    protected int unravel = 10;
    protected boolean timeToStopRotatingSrv = false;
    protected boolean timeToStopRotatingBuild = false;
    protected boolean timeToStopRotatingPage = false;

    protected Instance currentInstance;

    @Getter
    final CourtConfiguration cc;
    protected URLCreator urlCreator;
    protected Issue issue = null;
    protected Issue finalIssue = null;
    protected SearchRequest request = SearchRequest.getInstance();

    protected int indexUrl = 0;
    protected int build = 1;
    protected int prevSize = -1;
    private int prevSrvSize = -1;
    private int prevBuildSize = -1;

    @Getter
    protected Set<Case> resultCases = new HashSet<>();

    protected String[] urls;

    public SUDRFStrategy(CourtConfiguration cc) {
        this.cc = cc;

        setPage_num();

        if (cc.getSearchPattern() != SearchPattern.VNKOD_PATTERN) timeToStopRotatingBuild = true;
    }

    public SUDRFStrategy(CourtConfiguration cc, CourtStatus status) {
        this.cc = cc;

        this.page_num = status.page;

        this.srv_num = status.srv == null ? 1 : status.build;

        this.build = status.build == null ? 1 : status.build;

        if (cc.getSearchPattern() != SearchPattern.VNKOD_PATTERN) timeToStopRotatingBuild = true;
    }

    private void setPage_num() {
        if (cc.getSearchPattern() == SearchPattern.SECONDARY_PATTERN ||
                cc.getSearchPattern() == SearchPattern.DEPRECATED_SECONDARY_PATTERN) {
            page_num = 0;
        }
        else {
            page_num = 1;
        }
    }

    private void resetPage() {
        if (cc.getSearchPattern()!=SearchPattern.SECONDARY_PATTERN
                && cc.getSearchPattern()!=SearchPattern.DEPRECATED_SECONDARY_PATTERN) {
            urls[indexUrl] = urls[indexUrl].replace("page="+page_num,"page="+1);
        }
        else {
            urls[indexUrl] = urls[indexUrl].replace("\"start\":"+page_num,"\"start\":"+0);
        }
        setPage_num();
    }

    protected void rotate() {
        if (timeToStopRotatingPage) {
            if (cc.getStrategyName() == StrategyName.MOSGORSUD_STRATEGY) {
                timeToStopRotatingSrv = true;
                return;
            }
            resetPage();
            timeToStopRotatingPage = false;
            if (timeToStopRotatingBuild && (build > 1)) {
                urls[indexUrl] = urls[indexUrl].replace("num_build=" + build, "num_build=" + 1);
                build = 1;
                timeToStopRotatingBuild = false;
                prevSrvSize = resultCases.size();
                rotateSrv();
            } else if (timeToStopRotatingBuild) {
                prevSrvSize = resultCases.size();
                rotateSrv();
            } else {
                prevBuildSize = resultCases.size();
                rotateBuild();
            }
        }
        else {
            rotatePage();
        }
    }

    private void rotatePage() {
        if (cc.getSearchPattern()!=SearchPattern.SECONDARY_PATTERN
                && cc.getSearchPattern()!=SearchPattern.DEPRECATED_SECONDARY_PATTERN) {
            urls[indexUrl] = urls[indexUrl].replace("page="+page_num,"page="+(page_num+1));
            ++page_num;
        }
        else {
            urls[indexUrl] = urls[indexUrl].replace("\"start\":"+page_num,"\"start\":"+(page_num+10));
            page_num = page_num + 10;
        }
    }

    protected void clear() {
        timeToStopRotatingSrv = false;
        srv_num = 1;
        unravel = 10;
        if (cc.getSearchPattern() != SearchPattern.VNKOD_PATTERN) timeToStopRotatingBuild = true;
        else {
            timeToStopRotatingBuild = false;
            build = 1;
        }
        prevSize = -1;
        prevSrvSize = -1;
        setPage_num();
        currentDocument = null;
    }

    protected void rotateBuild() {
        if (cc.getSearchPattern()==SearchPattern.VNKOD_PATTERN) {
            urls[indexUrl]=urls[indexUrl].replace("num_build="+build,"num_build="+(build+1));
            ++build;
        }
    }

    protected void rotateSrv() {
        if (cc.getSearchPattern() != SearchPattern.SECONDARY_PATTERN
                && cc.getSearchPattern()!=SearchPattern.DEPRECATED_SECONDARY_PATTERN) {
            urls[indexUrl]=urls[indexUrl].replace("srv_num="+srv_num,"srv_num="+(srv_num+1));
            ++srv_num;
        } else {
            timeToStopRotatingSrv = true;
        }
    }
    protected void setVnkod(Document document) {
        if (cc.getSearchPattern()!=SearchPattern.SECONDARY_PATTERN &&
                cc.getSearchPattern()!=SearchPattern.DEPRECATED_SECONDARY_PATTERN) {
            ConfigurationHelper.setVnkodForNonSecondaryPatterns(cc,document);
        }
        else {
            setVnkodForSecondaryPattern();
        }
        if (cc.getVnkod() == null) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.VNKOD_NOT_FOUND+cc.getSearchString());
        }
    }

    private void setVnkodForSecondaryPattern() {
        try {
            ConfigurationHelper.findElementAndSetVnkod(cc);
        } catch (Exception ignored) {}
    }

    protected boolean checkConditions() {
        if (prevSize == resultCases.size()) {
            if (!timeToStopRotatingPage) {
                timeToStopRotatingPage = true;
            }
            if (srv_num > 2 && prevSrvSize == resultCases.size()) {
                timeToStopRotatingSrv = true;
                return true;
            } else {
                if (build > 2 && prevBuildSize == resultCases.size()) {
                    timeToStopRotatingBuild = true;
                    if (prevSrvSize == resultCases.size()) {
                        timeToStopRotatingSrv = true;
                        return true;
                    }
                }
                rotate();
            }
            return true;
        }
        return false;
    }

    private final IssueByTextIdentifier identifier = new IssueByTextIdentifier();

    protected void checkText(String text) {
        Issue identified = identifier.checkText(text, cc.getConnection());
        switch (identified) {
            case CAPTCHA, SUCCESS -> issue = identified;
            case URL_ERROR, NOT_FOUND_CASE, BLOCKED, NOT_FOUND, UNDEFINED_ISSUE -> {
                issue = identified;
                finalIssue = Issue.compareAndSetIssue(identified, finalIssue);
            }
            default -> {
                issue = identified;
                finalIssue = Issue.compareAndSetIssue(identified, finalIssue);
                unravel = unravel - 5;
            }
        }
    }

    protected boolean checkArticleAndInstance(Instance i) {
        return Arrays.stream(SearchRequest.getInstance().getField().getInstanceList()).collect(Collectors.toSet()).contains(i);
    }

    protected void createUrls(Instance instance) {
        switch (instance) {
            case FIRST -> createFirstInstanceUrls();
            case APPELLATION -> createAppellationUrls();
            case CASSATION -> createCassationUrls();
        }
    }

    private void createCassationUrls() {
        urlCreator = new URLCreator(cc, Instance.CASSATION);
        urls = urlCreator.createUrls();
    }

    private void createAppellationUrls() {
        urlCreator = new URLCreator(cc,Instance.APPELLATION);
        urls = urlCreator.createUrls();
    }

    private void createFirstInstanceUrls() {
        urlCreator = new URLCreator(cc);
        urls = urlCreator.createUrls();
    }

    protected Set<Case> filterCases() {
        String textToFind = request.getText();
        if (textToFind != null) {
            Set<Case> cases = new HashSet<>();
            for (Case _case:resultCases) {
                if (_case.getText() != null && _case.getText().contains(textToFind)) {
                    cases.add(_case);
                }
            }
            resultCases = cases;
            if (cases.isEmpty()) {
                issue = Issue.NOT_FOUND_CASE;
                finalIssue = Issue.NOT_FOUND_CASE;
            }
        }

        if (request.getArticle() != null && request.getArticle() instanceof SoftStrictFilterableArticle) {
            SoftStrictFilterer.SoftStrictMode softStrictMode = SoftStrictFilterer.SoftStrictMode.parseMode(
                    ApplicationConfiguration
                            .getInstance()
                            .getProperty("cases.article_filter")
            );

            if (softStrictMode == null) {
                SimpleLogger.log(LoggingLevel.WARNING, "SoftStrictMode is Malformed");
                return resultCases;
            }

            resultCases = resultCases
                    .stream()
                    .filter(
                        new SoftStrictFilterer(request.getArticle().getMainPart(), softStrictMode)
                    )
                    .collect(Collectors.toSet());

            if (resultCases.isEmpty()) {
                issue = Issue.NOT_FOUND_CASE;
                finalIssue = Issue.NOT_FOUND_CASE;
            }
        }
        return resultCases;
    }

    protected void finish() {
        setFinalInfo();
        logFinalInfo();
    }

    protected void logFinalInfo() {
        if (!Issue.isGoodIssue(cc.getIssue()) && !Issue.isBadIssue(cc.getIssue())) {
            SimpleLogger.log(LoggingLevel.DEBUG,cc.getIssue() + " " + urls[indexUrl]);
        }
        else if (Issue.isBadIssue(cc.getIssue())) {
            SimpleLogger.log(LoggingLevel.WARNING, cc.getIssue() + " " + urls[indexUrl]);
        }

        try {
            SimpleLogger.addToCourtHistory(cc, resultCases.size());
        } catch (IOException e) {
            SimpleLogger.log(LoggingLevel.ERROR, String.format(Message.IOEXCEPTION_OCCURRED.toString(), e));
        }
    }

    protected void checkIfWorkingUrlNotWorking() {
        if ((finalIssue != Issue.SUCCESS && finalIssue != Issue.NOT_FOUND_CASE
                && finalIssue != Issue.NOT_SUPPORTED_REQUEST && finalIssue != Issue.CONNECTION_ERROR)
                && cc.getWorkingUrl().get(request.getField()) != null) {
            cc.putWorkingUrl(request.getField(),null);
        }
    }

    protected void setFinalInfo() {
        cc.setIssue(Objects.requireNonNullElseGet(finalIssue, () -> Objects.requireNonNullElse(issue, Issue.ERROR)));

        if (cc.isHasCaptcha()) return;

        checkIfWorkingUrlNotWorking();

        putWorkingUrl();
    }

    private void putWorkingUrl() {
        if (Issue.isGoodIssue(finalIssue) && cc.getWorkingUrl().get(request.getField()) == null
                && currentInstance == Instance.FIRST) {
            cc.putWorkingUrl(request.getField(), urlCreator.returnEnding(indexUrl));
        }
    }
}
