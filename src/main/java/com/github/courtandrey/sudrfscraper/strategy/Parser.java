package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.dump.model.Case;
import com.github.courtandrey.sudrfscraper.service.CasesPipeLine;
import org.jsoup.nodes.Document;

import java.util.Set;

public interface Parser {
    boolean isTextFound();
    Set<Case> scrap(Document document, String currentUrl);

    Set<Case> scrapTexts(Set<Case> resultCases);
    String parseText(Document decision);

    void scrapTextsAndFlush(Set<Case> resultCases, CasesPipeLine casesPipeLine);
}
