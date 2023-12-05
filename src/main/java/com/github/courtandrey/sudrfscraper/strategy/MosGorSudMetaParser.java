package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.dump.model.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

public class MosGorSudMetaParser implements MetaParser {
    private static final String PRE_HTTPS = "https://www.mos-gorsud.ru";
    private static final String PRE_HTTP = "https://www.mos-gorsud.ru";
    private final static List<String> caseUniqueIds = List.of("Уникальный идентификатор дела", "УИД");
    private final static List<String> sides = List.of("Стороны");
    private final static List<String> historyOfEvents = List.of("История состояний");
    private final static List<String> consideredByMarkers = List.of("Номер судебного состава");
    private final static List<String> connectedLinkUpper = List.of("Номер дела в суде вышестоящей инстанции");
    private final static List<String> connectedLinkLower = List.of("Номер дела в суде нижестоящей инстанции");

    @Override
    public CaseParsingResultBox parseMeta(Case aCase, Document document) {
        Elements elements = document.getElementsByClass("row_card");

        for (Element el:elements) {
            Elements left = el.getElementsByClass("left");
            if (left.isEmpty()) continue;

            String leftText = left.text().trim();

            if (caseUniqueIds.contains(leftText)) {
                Elements right = el.getElementsByClass("right");
                if (!right.isEmpty()) aCase.setCUI(right.text());
            }

            if (sides.contains(leftText)) {
                Elements right = el.getElementsByTag("right");
                if (!right.isEmpty()) {
                    String rightHtml = right.html();
                    String[] firstSplit = rightHtml.split("<br>");

                    for (String split:firstSplit) {
                        Side side = new Side();
                        side.setSideType(split.split("</strong>")[0].replace(":","")
                                .replace("<strong>","").trim());

                        side.setSideName(split.split("</strong>")[1].trim());
                        aCase.addSide(side);
                    }
                }
            }

            if (consideredByMarkers.contains(leftText)) {
                Elements right = el.getElementsByClass("right");
                if (!right.isEmpty()) aCase.setConsideredBy("Номер судебного состава: " + right.text());
            }

            if (connectedLinkUpper.contains(leftText)) {
                Elements right = el.getElementsByClass("right");
                if (!right.isEmpty()) {
                    Elements href = right.get(0).getElementsByTag("a");
                    if (!href.isEmpty()) {
                        String hrefLink = href.get(0).attr("href");
                        if (hrefLink.contains(PRE_HTTP)) {
                            aCase.addConnectedLink(ConnectionType.LINK_UPPER, hrefLink);
                        } else if (hrefLink.contains(PRE_HTTPS)) {
                            aCase.addConnectedLink(ConnectionType.LINK_UPPER, hrefLink.replace(PRE_HTTPS, PRE_HTTP));
                        } else {
                            aCase.addConnectedLink(ConnectionType.LINK_UPPER, PRE_HTTP + hrefLink);
                        }
                    } else {
                        aCase.addConnectedLink(ConnectionType.CASE_NUMBER_UPPER, href.text());
                    }
                }
            }
            if (connectedLinkLower.contains(leftText)) {
                Elements right = el.getElementsByClass("right");
                if (!right.isEmpty()) {
                    Elements href = right.get(0).getElementsByTag("a");
                    if (!href.isEmpty()) {
                        String hrefLink = href.get(0).attr("href");
                        if (hrefLink.contains(PRE_HTTP)) {
                            aCase.addConnectedLink(ConnectionType.LINK_LOWER, hrefLink);
                        } else if (hrefLink.contains(PRE_HTTPS)) {
                            aCase.addConnectedLink(ConnectionType.LINK_LOWER, hrefLink.replace(PRE_HTTPS, PRE_HTTP));
                        } else {
                            aCase.addConnectedLink(ConnectionType.LINK_LOWER, PRE_HTTP + hrefLink);
                        }
                    } else {
                        aCase.addConnectedLink(ConnectionType.CASE_NUMBER_LOWER, href.text());
                    }
                }
            }
        }

        Element id = document.getElementById("state-history");
        if (id != null) {
            Elements headers = id.getElementsByTag("h3");
            Integer elementId = null;

            if (!headers.isEmpty()) {
                for (int i = 0; i < headers.size(); i++) {
                    if (historyOfEvents.contains(headers.get(i).text())) {
                        elementId = i;
                        break;
                    }
                }
            }

            if (elementId != null) {
                Elements tables = id.getElementsByTag("tbody");
                if (tables.size() > elementId) {
                    Element table = tables.get(elementId);

                    for (Element el:table.getElementsByTag("tr")) {
                        Step step = new Step();
                        Elements tds = el.getElementsByTag("td");

                        step.setEventDate(tds.get(0).text().trim().isEmpty() ? null : tds.get(0).text().trim());
                        step.setEventName(tds.get(1).text().trim().isEmpty() ? null : tds.get(1).text().trim());
                        step.setEventResultReason(tds.get(2).text().trim().isEmpty() ? null : tds.get(2).text().trim());
                        aCase.addStep(step);
                    }
                }
            }
        }

        if (aCase.getCUI() == null && aCase.getConnectedLinks().isEmpty() &&
                aCase.getSides().isEmpty() && aCase.getSteps().isEmpty() && aCase.getConsideredBy() == null)
            return new CaseParsingResultBox(CaseParsingResult.CASE_COULD_NOT_BE_PARSED, aCase);

        return new CaseParsingResultBox(CaseParsingResult.SUCCESS, aCase);
    }
}
