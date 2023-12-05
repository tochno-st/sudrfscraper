package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.dump.model.Case;
import org.jsoup.nodes.Document;

public interface MetaParser {
    CaseParsingResultBox parseMeta(Case aCase, Document document);

    default CaseParsingResultBox nonParsableBox(Case aCase) {
        return new CaseParsingResultBox(CaseParsingResult.CASE_COULD_NOT_BE_PARSED, aCase);
    }
}
