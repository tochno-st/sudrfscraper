package com.github.courtandrey.sudrfscraper.service;

import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.*;
import com.github.courtandrey.sudrfscraper.service.logger.LoggingLevel;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;
import com.github.courtandrey.sudrfscraper.strategy.Connection;
import lombok.experimental.UtilityClass;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@UtilityClass
public class ConfigurationHelper {
    public void getStrategy(CourtConfiguration cc) {
        if (cc.getSearchPattern() == null) cc.setSearchPattern(SearchPattern.PRIMARY_PATTERN);
        configureExceptions(cc);
        if (cc.isHasCaptcha()) {
            cc.setStrategyName(StrategyName.CAPTCHA_STRATEGY);
        }
        else if (cc.getSearchPattern() != SearchPattern.MOSGORSUD_PATTERN){
            cc.setStrategyName(StrategyName.PRIMARY_STRATEGY);
        }
    }

    public void reset(List<CourtConfiguration> ccs) {
        reset(ccs,false);
    }

    public void reset(List<CourtConfiguration> ccs, boolean deleteWorkingUrl) {
        ccs.forEach(x-> resetCC(x,deleteWorkingUrl));
    }

    private void resetCC(CourtConfiguration cc, boolean deleteWorkingUrl) {
        cc.setIssue(null);
        getStrategy(cc);
        if (deleteWorkingUrl)
            cc.getWorkingUrl().keySet().forEach(x -> cc.putWorkingUrl(x,null));
    }

    public void findElementAndSetVnkod(CourtConfiguration cc) {
        SimpleLogger.println(Message.GO_TO_ANOTHER_PAGE);
        SystemHelper.doBeeps();
        SeleniumHelper sh = SeleniumHelper.getInstance();
        String prevUrl = sh.getCurrentUrl();
        String currentUrl = sh.getCurrentUrl();
        while (currentUrl.equals(prevUrl)) {
            ThreadHelper.sleep(1);
            currentUrl = sh.getCurrentUrl();
        }
        if (currentUrl.contains("filterValue")) {
            cc.setVnkod(currentUrl.split("%22filterValue%22:%22")[1].substring(0,8));
        }
    }

    public synchronized void setVnkodForNonSecondaryPatterns(CourtConfiguration cc, Document document) {
        Set<String> vnkods = new HashSet<>();
        Elements elements = document.getElementsByAttribute("href");
        List<String> hrefs = new ArrayList<>();
        for (Element element:elements) {
            if (element.attr("href").contains("vnkod")) hrefs.add(element.attr("href"));
        }
        if (hrefs.size() > 1) {
            for (int i = 1; i<hrefs.size(); i++) {
                vnkods.add(hrefs.get(i).split("vnkod=")[1].substring(0, 8));
            }
            if (vnkods.size() > 1) {
                SimpleLogger.log(LoggingLevel.DEBUG, Message.MANY_VNKODS + cc.getSearchString());
            }
            cc.setVnkod((String) vnkods.toArray()[0]);
        } else {
            SimpleLogger.log(LoggingLevel.WARNING, Message.VNKOD_MISSING +" "+ cc.getSearchString());
        }
    }

    public void configureExceptions(CourtConfiguration cc) {
        if (cc.getSearchPattern() == SearchPattern.MOSGORSUD_PATTERN) {
            cc.setStrategyName(StrategyName.MOSGORSUD_STRATEGY);
        }
        if (cc.getSearchPattern() == SearchPattern.SECONDARY_PATTERN || cc.getSearchPattern() == SearchPattern.DEPRECATED_SECONDARY_PATTERN) {
            cc.setSearchPattern(SearchPattern.PRIMARY_PATTERN);
            cc.setConnection(Connection.REQUEST);
        }
        if (cc.getRegion() == 36 && cc.getLevel() != Level.REGION) {
            cc.setConnection(Connection.SELENIUM);
        }
        if (cc.getSearchString().equals("http://kraevoi--krd.sudrf.ru")
                ||cc.getSearchString().equals("http://oblsud--orl.sudrf.ru")) {
            cc.setConnection(Connection.SELENIUM);
            cc.setSearchPattern(SearchPattern.VNKOD_PATTERN);
        }
    }

    public void analyzeIssues(List<CourtConfiguration> ccs) {
        ccs.forEach(ConfigurationHelper::analyzeIssue);
    }

    private void analyzeIssue(CourtConfiguration cc) {
        if (cc.getIssue() == null) {
            getStrategy(cc);
        }
        else {
            switch (cc.getIssue()) {
                case SUCCESS, NOT_FOUND_CASE -> cc.setStrategyName(StrategyName.END_STRATEGY);
                case CAPTCHA -> {
                    cc.setIssue(null);
                    cc.setHasCaptcha(true);
                    cc.setStrategyName(StrategyName.CAPTCHA_STRATEGY);
                }
                default -> getStrategy(cc);
            }
        }
    }


    public String wrapIssues(List<CourtConfiguration> ccs) {
        StringBuilder text = new StringBuilder();
        for (Issue issue:Issue.values()) {
            List<CourtConfiguration> ccsWithIssue = get(issue, ccs);
            text.append(issue).append(": ").append(ccsWithIssue.size()).append("\n");
            if (issue != Issue.SUCCESS && issue != Issue.NOT_FOUND_CASE) {
                for (CourtConfiguration cc: ccsWithIssue) {
                    text.append(cc.toString()).append("\n");
                }
            }
        }
        return text.toString();
    }

    private List<CourtConfiguration> get(Issue issue, List<CourtConfiguration> ccs) {
        List<CourtConfiguration> neededCC = new ArrayList<>();
        for (CourtConfiguration cc: ccs) {
            if (cc.getIssue() == issue) {
                neededCC.add(cc);
            }
        }
        return neededCC;
    }

    public boolean checkVnkods(List<CourtConfiguration> ccs) {
        boolean isVnkodPlaced = true;
        for (CourtConfiguration cc:ccs) {
            if (cc.getVnkod() == null) {
                SimpleLogger.log(LoggingLevel.WARNING, Message.VNKOD_MISSING +" "+ cc.getSearchString());
                isVnkodPlaced = false;
            }
        }
      return isVnkodPlaced;
    }

}
