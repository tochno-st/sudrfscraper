package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.dump.model.Case;
import lombok.Getter;

@Getter
public class CaseParsingResultBox {
    private final CaseParsingResult cps;
    private final Case aCase;

    public CaseParsingResultBox(CaseParsingResult cps, Case aCase) {
        this.cps = cps;
        this.aCase = aCase;
    }
}
