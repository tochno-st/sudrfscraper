package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.CourtConfiguration;
import com.github.courtandrey.sudrfscraper.dump.model.Case;
import com.github.courtandrey.sudrfscraper.service.Converter;
import com.github.courtandrey.sudrfscraper.service.Downloader;
import com.github.courtandrey.sudrfscraper.service.logger.LoggingLevel;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

public class MosGorSudParser extends ConnectorParser{
    private final Set<Case> cases = new HashSet<>();
    protected boolean isTextFound = false;
    private final Downloader downloader = new Downloader();
    private final Converter converter = new Converter();
    MosGorSudParser(CourtConfiguration cc) {
        super(cc);
    }
    @Override
    public boolean isTextFound() {
        return isTextFound;
    }

    @Override
    public Set<Case> scrap(Document document, String currentUrl) {
        Elements table = document.getElementsByClass("custom_table");
        if (table.isEmpty()) return cases;
        for (Element element:table.get(0).getElementsByTag("tr")) {
            try{
                if (!element.getElementsByTag("th").isEmpty()) continue;
                Elements rowParts = element.getElementsByTag("td");
                Case _case = new Case();
                _case.setCaseNumber(rowParts.get(0).getElementsByTag("nobr").get(0).text());
                _case.setName(cc.getName());
                _case.setNames(rowParts.get(1).text()+"&"+rowParts.get(4).text()+"&"+rowParts.get(5).text());
                _case.setDecision(rowParts.get(2).text());
                _case.setJudge(rowParts.get(3).text());
                _case.setRegion(cc.getRegion());
                _case.setText(currentUrl+rowParts.get(0).getElementsByTag("a").attr("href"));
                cases.add(_case);
            } catch (Exception e) {
                SimpleLogger.log(LoggingLevel.DEBUG, Message.MOSGORSUD_PARSING_EXCEPTION + e.getLocalizedMessage());
            }
        }
        return cases;
    }

    @Override
    public Set<Case> scrapTexts(Set<Case> resultCases) {
        if (resultCases.isEmpty()) return resultCases;
        SimpleLogger.log(LoggingLevel.INFO, String.format(
                Message.COLLECTING_TEXTS.toString(),
                resultCases.size(),
                cc.getName())
        );
        int i = 1;
        Set<Case> newCases = new HashSet<>();
        for (Case _case:resultCases) {
            String url = _case.getText();
            if (url != null) {
                _case.setText(null);
                try{
                    String text = getJsoupText(url);
                    if (text != null) {
                        isTextFound = true;
                        String[] splits = text.split("\\$DELIMITER");
                        if (splits.length == 1) {
                            if (!text.equals("MALFORMED"))
                                _case.setText(text);
                            else
                                _case.setText(null);
                        }
                        else {
                            _case.setText(splits[0]);
                            for (int j = 1; j < splits.length; j++) {
                                Case newCase = getaCase(_case, j, splits);
                                newCases.add(newCase);
                            }

                            _case.setCaseNumber(_case.getCaseNumber() + " ("+0+")");
                        }
                    }
                }catch (Exception e) {
                    SimpleLogger.log(LoggingLevel.DEBUG, Message.DOCUMENT_NOT_PARSED + url);
                }
            }
            if (i % 25 == 0) {
                SimpleLogger.log(LoggingLevel.INFO, String.format(Message.COLLECTED_TEXTS.toString(), i, resultCases.size(), cc.getName()));
            }
            i += 1;
        }
        resultCases.addAll(newCases);
        return resultCases;
    }

    private Case getaCase(Case _case, int j, String[] splits) {
        Case newCase = new Case();
        newCase.setCaseNumber(_case.getCaseNumber()+" ("+ j +")");
        newCase.setNames(_case.getNames());
        newCase.setJudge(_case.getJudge());
        newCase.setRegion(_case.getRegion());
        newCase.setName(_case.getName());
        newCase.setDecision(_case.getDecision());
        newCase.setText(splits[j]);
        return newCase;
    }

    @Override
    public String parseText(Document decision) {
        if (decision.getElementsByAttributeValue("id", "tabs-3").isEmpty()) return null;
        Element table = decision.getElementsByAttributeValue("id", "tabs-3").get(0);
        StringBuilder stringBuilder = new StringBuilder();
        for (Element e : table.getElementsByTag("tr")) {
            if (!e.getElementsByTag("th").isEmpty()) continue;
            Element textElement = e.getElementsByTag("td").get(2);
            if (textElement.getElementsByTag("a").attr("href").isEmpty()) continue;
            String url = cc.getSearchString() + textElement.getElementsByTag("a").attr("href");

            if (url.equalsIgnoreCase("https://www.mos-gorsud.ru#") || url.equalsIgnoreCase("http://www.mos-gorsud.ru#")) {
                if (stringBuilder.isEmpty()) stringBuilder.append("MALFORMED");
                continue;
            }
            String text = converter.getTxtFromFile(downloader.download(url));
            if (text == null) continue;
            text = cleanUp(text);
            if (!stringBuilder.isEmpty()) {
                if (stringBuilder.toString().equals("MALFORMED")) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(text);
                } else {
                    stringBuilder.append("$DELIMITER").append(text);
                }
            } else {
                stringBuilder.append(text);
            }
        }
        if (stringBuilder.isEmpty()) return null;
        return stringBuilder.toString();
    }


}
