package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.SearchPattern;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Instance;
import com.github.courtandrey.sudrfscraper.dump.model.*;
import com.github.courtandrey.sudrfscraper.service.logger.LoggingLevel;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class GeneralMetaParser implements MetaParser {
    private final SearchPattern searchPattern;
    public GeneralMetaParser(SearchPattern searchPattern) {
        this.searchPattern = searchPattern;
    }

    @Override
    public CaseParsingResultBox parseMeta(Case aCase, Document document) {
        CaseParsingResultBox box;
        switch (searchPattern) {
            case VNKOD_PATTERN -> box = parseMetaForVnkod(aCase, document);
            case PRIMARY_PATTERN -> box = parseMetaForGeneral(aCase,document);
            default -> {
                SimpleLogger.log(LoggingLevel.WARNING, "Couldn't find suitable metaParsing strategy for: " + searchPattern.name());
                return new CaseParsingResultBox(CaseParsingResult.CASE_COULD_NOT_BE_PARSED,aCase);
            }
        }
        if (box.getCps() == CaseParsingResult.SUCCESS) return box;

        if (checkText(document)) return new CaseParsingResultBox(CaseParsingResult.COURT_DOWN, box.getACase());

        return box;
    }

    private boolean checkText(Document document) {
        String text = document.text();
        return text.contains("Информация временно недоступна") || text.contains("Warning: pg_connect():") || text.contains("В настоящий момент производится информационное наполнение сайта. Обратитесь к странице позже");

    }

    private CaseParsingResultBox parseMetaForVnkod(Case aCase, Document document) {
        Element contentCase = document.getElementById("tab_content_Case");
        if (contentCase == null) {
            SimpleLogger.log(LoggingLevel.DEBUG, "Couldn't parse CUI for: " + aCase.getLinks().get(LinkType.META));
        } else {
            Elements trs = contentCase.getElementsByTag("tr");
            for (Element e:trs) {
                if (e.text().contains("Уникальный идентификатор дела")) {
                    if (e.getElementsByTag("td").size() > 1)
                        aCase.setCUI(e.getElementsByTag("td").get(1).text());
                    else {
                        aCase.setCUI(e.text());
                    }
                }

                if (e.text().contains("Признак рассмотрения дела")) {
                    if (e.getElementsByTag("td").size() > 1)
                        aCase.setConsideredBy(e.getElementsByTag("td").get(1).text());
                    else {
                        aCase.setConsideredBy(e.text());
                    }
                }
            }
        }

        Element contentEvent = document.getElementById("tab_content_EventList");
        if (contentEvent == null) {
            SimpleLogger.log(LoggingLevel.DEBUG, "Couldn't parse events for: " + aCase.getLinks().get(LinkType.META));
        } else {
            Elements thead = contentEvent.getElementsByTag("thead");
            List<String> heads = new ArrayList<>();
            if (!thead.isEmpty()) {
                for (Element e : thead.get(0).getElementsByTag("td")) {
                    heads.add(e.text());
                }
                Elements trs = contentEvent.getElementsByTag("tr");
                for (Element e:trs) {
                    Step step = new Step();
                    Elements tds = e.getElementsByTag("td");
                    for (int i = 0; i < tds.size(); i++) {
                        matchAndSetForStep(tds.get(i).text(), heads.get(i), step);
                    }
                    aCase.addStep(step);
                }
            } else {
                SimpleLogger.log(LoggingLevel.DEBUG, "Couldn't find heads in event section: " + aCase.getLinks().get(LinkType.META));
            }
        }


        Element contentPerson = document.getElementById("tab_id_PersonList");
        if (contentPerson == null) {
            SimpleLogger.log(LoggingLevel.DEBUG, "Couldn't parse sides for: " + aCase.getLinks().get(LinkType.META));
        } else {
            Elements thead = contentPerson.getElementsByTag("thead");
            List<String> heads = new ArrayList<>();
            if (!thead.isEmpty()) {
                for (Element e : thead.get(0).getElementsByTag("td")) {
                    heads.add(e.text());
                }
                Elements trs = contentPerson.getElementsByTag("tr");
                for (Element e:trs) {
                    Side side = new Side();
                    Elements tds = e.getElementsByTag("td");
                    for (int i = 0; i < tds.size(); i++) {
                        matchAndSetForSide(tds.get(i).text(), heads.get(i), side);
                    }
                    aCase.addSide(side);
                }
            } else {
                SimpleLogger.log(LoggingLevel.DEBUG, "Couldn't find heads in Sides section: " + aCase.getLinks().get(LinkType.META));
            }
        }

        if (aCase.getCUI() == null && aCase.getSteps().isEmpty() &&
                aCase.getSides().isEmpty() && aCase.getConsideredBy() == null) return nonParsableBox(aCase);

        if (aCase.getCUI() == null) SimpleLogger.log(LoggingLevel.DEBUG, "Couldn't find a CUI for: " + aCase.getLinks().get(LinkType.META));

        return new CaseParsingResultBox(CaseParsingResult.SUCCESS, aCase);
    }

    private void matchAndSetForSide(String src, String trg, Side side) {
        if (src.isEmpty()) return;
        switch (trg) {
            case "Вид лица, участвующего в деле" -> side.setSideType(src);
            case "Лицо, участвующее в деле (ФИО, наименование)", "Фамилия / наименование" -> side.setSideName(src);
            case "ИНН" -> side.setINN(src);
            case "КПП" -> side.setKPP(src);
            case "ОГРН" -> side.setOGRN(src);
            case "ОГРНИП" -> side.setOGRNIP(src);
            default -> SimpleLogger.log(LoggingLevel.WARNING, "Couldn't match property for Side: " + trg);
        }
    }

    private void matchAndSetForStep(String src, String trg, Step step) {
        if (src.isEmpty()) return;
        switch (trg) {
            case "Наименование события" -> step.setEventName(src);
            case "Дата", "Дата события" -> step.setEventDate(src);
            case "Время", "Время слушания", "Время события" -> step.setEventTime(src);
            case "Место проведения" -> step.setEventPlace(src);
            case "Результат события" -> step.setEventResult(src);
            case "Основание для выбранного результата события", "Основания для выбранного результата события" -> step.setEventResultReason(src);
            case "Примечание" -> step.setAdditionalInfo(src);
            default -> {
                if (trg.contains("Дата размещения")) {
                    step.setPublishedDate(src);
                }
                else {
                    SimpleLogger.log(LoggingLevel.WARNING, "Couldn't match property for Step: " + trg);
                }
            }
        }

    }



    private CaseParsingResultBox parseMetaForGeneral(Case aCase, Document document) {
        Element contentCase = document.getElementById("cont1");
        if (contentCase == null) {
            SimpleLogger.log(LoggingLevel.DEBUG, "Couldn't parse CUI for: " + aCase.getLinks().get(LinkType.META));
        } else {
            Elements trs = contentCase.getElementsByTag("tr");
            for (Element e:trs) {
                if (e.text().contains("Уникальный идентификатор дела")) {
                    if (e.getElementsByTag("td").size() > 1)
                        aCase.setCUI(e.getElementsByTag("td").get(1).text());
                    else {
                        aCase.setCUI(e.text());
                    }
                }

                if (e.text().contains("Признак рассмотрения дела")) {
                    if (e.getElementsByTag("td").size() > 1)
                        aCase.setConsideredBy(e.getElementsByTag("td").get(1).text());
                    else {
                        aCase.setConsideredBy(e.text());
                    }
                }
            }
        }

        Elements tbodies = document.getElementsByTag("tbody");
        if (tbodies.isEmpty()) {
            SimpleLogger.log(LoggingLevel.WARNING, "No tbodies found: " + aCase.getLinks().get(LinkType.META));
        } else {
            boolean isHeadFound = false;
            for (Element el:tbodies) {
                if (el.getElementsByTag("th").text().contains("РАССМОТРЕНИЕ В НИЖЕСТОЯЩЕМ СУДЕ")
                        && aCase.getInstance().equals(Instance.CASSATION.name().toUpperCase())) {
                    setRegionForCassation(el, aCase);
                }
                if (isHeadFound) break;
                Elements trs = el.getElementsByTag("tr");
                List<String> heads = new ArrayList<>();
                if (!trs.isEmpty()) {
                    for (Element tr : trs) {
                        if (isHeadFound) {
                            Step step = new Step();
                            Elements tds = tr.getElementsByTag("td");
                            for (int j = 0; j < tds.size(); j++) {
                                matchAndSetForStep(tds.get(j).text(), heads.get(j), step);
                            }
                            aCase.addStep(step);
                        } else {
                            if (tr.text().contains("Наименование события")) {
                                for (Element e : tr.getElementsByTag("td")) {
                                    heads.add(e.text());
                                }
                                isHeadFound = true;
                            }
                        }
                    }
                }
            }
            if (!isHeadFound)
                SimpleLogger.log(LoggingLevel.DEBUG, "Couldn't find heads events section: " + aCase.getLinks().get(LinkType.META));
            isHeadFound = false;
            for (Element el:tbodies) {
                if (isHeadFound) break;
                Elements trs = el.getElementsByTag("tr");
                List<String> heads = new ArrayList<>();
                if (!trs.isEmpty()) {
                    for (Element tr : trs) {
                        if (isHeadFound) {
                            Side side = new Side();
                            Elements tds = tr.getElementsByTag("td");
                            for (int j = 0; j < tds.size(); j++) {
                                matchAndSetForSide(tds.get(j).text(), heads.get(j), side);
                            }
                            aCase.addSide(side);
                        } else {
                            if (tr.text().contains("Вид лица")) {
                                for (Element e : tr.getElementsByTag("td")) {
                                    heads.add(e.text());
                                }
                                isHeadFound = true;
                            }
                        }
                    }
                }
            }

            if (aCase.getCUI() == null) {
                for (Element el:tbodies) {
                    Elements trs = el.getElementsByTag("tr");
                    if (!trs.isEmpty()) {
                        for (Element tr : trs) {
                            if (tr.text().contains("Номер дела в первой инстанции") && tr.getElementsByTag("td").size() > 1) {
                                aCase.addConnectedLink(ConnectionType.CASE_NUMBER_LOWER,
                                        tr.getElementsByTag("td").get(1).text());
                            }
                        }
                    }
                }
            }

            if (!isHeadFound)
                SimpleLogger.log(LoggingLevel.DEBUG, "Couldn't find heads sides section: " + aCase.getLinks().get(LinkType.META));
        }

        //Номер дела в первой инстанции

        if (aCase.getCUI() == null && aCase.getSteps().isEmpty() &&
                aCase.getSides().isEmpty() && aCase.getConsideredBy() == null) return nonParsableBox(aCase);

        if (aCase.getCUI() == null) SimpleLogger.log(LoggingLevel.DEBUG, "Couldn't find a CUI for: " + aCase.getLinks().get(LinkType.META));

        return new CaseParsingResultBox(CaseParsingResult.SUCCESS, aCase);
    }

    private void setRegionForCassation(Element element, Case aCase) {
        for (Element row : element.select("tr")) {
            Elements cells = row.select("td");
            if (cells.size() == 2 && isCellsContainRegionText(cells)) {
                String s = cells.get(1).text();

                String[] parts = s.split(" ");

                aCase.setRegion(Integer.parseInt(parts[0]));

                break;
            }
        }
    }

    private static boolean isCellsContainRegionText(Elements cells) {
        return cells.get(0).text().contains("Регион нижестоящего суда")
                || cells.get(0).text().contains("Регион суда первой инстанции");
    }
}
