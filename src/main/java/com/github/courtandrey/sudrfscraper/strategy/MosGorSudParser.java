package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.configuration.ApplicationConfiguration;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.CourtConfiguration;
import com.github.courtandrey.sudrfscraper.dump.model.Case;
import com.github.courtandrey.sudrfscraper.dump.model.LinkType;
import com.github.courtandrey.sudrfscraper.exception.CourtIsDownException;
import com.github.courtandrey.sudrfscraper.service.Converter;
import com.github.courtandrey.sudrfscraper.service.Downloader;
import com.github.courtandrey.sudrfscraper.service.logger.LoggingLevel;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;
import org.apache.http.NoHttpResponseException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MosGorSudParser extends ConnectorParser{
    protected boolean isTextFound = false;
    private final Downloader downloader = new Downloader();
    private final Converter converter = new Converter();
    private final boolean showConflicts = Boolean.parseBoolean(
            ApplicationConfiguration.getInstance().getProperty("dev.show_mgs_conflicts")
    );
    private final MetaParser metaParser = new MosGorSudMetaParser();

    MosGorSudParser(CourtConfiguration cc) {
        super(cc);
    }

    @Override
    public boolean isTextFound() {
        return isTextFound;
    }

    @Override
    public Set<Case> scrap(Document document, String currentUrl) {
        Set<Case> cases = new HashSet<>();
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
                _case.setTextUrl(currentUrl+rowParts.get(0).getElementsByTag("a").attr("href"));
                _case.addLink(LinkType.TEXT_AND_META, currentUrl+rowParts.get(0).getElementsByTag("a").attr("href"));
                cases.add(_case);
            } catch (Exception e) {
                SimpleLogger.log(LoggingLevel.DEBUG, Message.MOSGORSUD_PARSING_EXCEPTION + e.getLocalizedMessage());
            }
        }
        return cases;
    }

    private Case workingCase = null;

    @Override
    public Set<Case> scrapTexts(Set<Case> resultCases) {
        if (resultCases.isEmpty()) return resultCases;
        SimpleLogger.log(LoggingLevel.INFO, String.format(
                Message.COLLECTING_TEXTS.toString(),
                resultCases.size(),
                cc.getName())
        );
        int i = 1;
        for (Case _case:resultCases) {
            workingCase = _case;
            String url = _case.getLinks().get(LinkType.TEXT_AND_META);
            if (url != null) {
                try{
                    Document doc = connector.getDocument(url);
                    String text = parseText(doc);
                    if (text != null) {
                        isTextFound = true;
                        String[] splits = text.split("\\$DELIMITER");
                        if (text.equals("MALFORMED") ) {
                            _case.setText(null);
                        }
                        else {
                            for (String split : splits) {
                                if (checkText(split, _case.getCaseNumber()))
                                    _case.setText(split.split("CELLAR_TEXT")[2]);
                            }
                            if (_case.getText() == null && showConflicts)
                                SimpleLogger.log(LoggingLevel.DEBUG, "Couldn't match case: " + Arrays.toString(splits));
                        }
                    }
                    CaseParsingResultBox box = metaParser.parseMeta(_case, doc);
                    if (box.getCps() == CaseParsingResult.COURT_DOWN) throw new CourtIsDownException();
                    if (box.getCps() == CaseParsingResult.CASE_COULD_NOT_BE_PARSED) throw new Exception("CUI NOT SET");
                } catch (CourtIsDownException | NoHttpResponseException e) {
                    SimpleLogger.log(LoggingLevel.DEBUG, "Court is possibly down. Couldn't parse text: " + url);
                } catch (Exception e) {
                    SimpleLogger.log(LoggingLevel.WARNING, "Meta was not parsed: " + url + " cause: " + e);
                }
            }
            if (i % 25 == 0) {
                SimpleLogger.log(LoggingLevel.INFO, String.format(Message.COLLECTED_TEXTS.toString(), i, resultCases.size(), cc.getName()));
            }
            i += 1;
        }
        return resultCases;
    }

    private boolean checkText(String text, String number) {
        String preparedNumber = number.split("/")[0];
        String preparedText = text.replaceAll(" ", "");
        int i;
        try {
            i = Integer.parseInt(preparedNumber.split("-")[1]);
        } catch (Exception e) {
            SimpleLogger.log(LoggingLevel.WARNING, "Couldn't parse MGS case number: " + number);
            return false;
        }
        if (preparedText.contains(preparedNumber)) return true;

        return preparedText.contains(preparedNumber.split("-")[0] + "-" + i);
    }

    private String extractRegistrationDate(Document decision) {
        if (decision.getElementsByClass("row_card").isEmpty()) return null;

        for (Element e : decision.getElementsByClass("row_card")) {
            if (e.getElementsByClass("left").isEmpty() ||
                !(e.getElementsByClass("left").get(0).text().contains("Дата регистрации") ||
                        e.getElementsByClass("left").get(0).text().contains("Дата поступления"))) continue;

            return e.getElementsByClass("right").get(0).text();
        }

        return null;
    }

    private String extractResultDate(Document decision) {
        if (decision.getElementsByClass("row_card").isEmpty()) return null;

        for (Element e : decision.getElementsByClass("row_card")) {
            if (e.getElementsByClass("left").isEmpty() ||
                    !e.getElementsByClass("left").get(0).text().contains("Дата вступления решения в силу")) continue;

            return e.getElementsByClass("right").get(0).text();
        }

        return null;
    }


    @Override
    public String parseText(Document decision) {
        workingCase.setEntryDate(extractRegistrationDate(decision));
        workingCase.setResultDate(extractResultDate(decision));
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

            text += "$CELLAR_TEXT" + textElement.text() + "$CELLAR_TEXT" + text;

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
