package com.github.courtandrey.sudrfscraper.service;

import com.github.courtandrey.sudrfscraper.dump.Updater;

public class CasesPipeLineFactory {
    private final Updater updater;
    private static CasesPipeLineFactory casesPipeLineFactory;

    private CasesPipeLineFactory(Updater updater) {
        this.updater = updater;
    }

    public static void init(Updater updater) {
        if (casesPipeLineFactory != null) return;
        casesPipeLineFactory = new CasesPipeLineFactory(updater);
    }

    public static synchronized CasesPipeLineFactory getInstance() {
        if (casesPipeLineFactory != null) return casesPipeLineFactory;
        throw new UnsupportedOperationException("CasesPipeLineFactory is not initialized");
    }

    public CasesPipeLine getPipeLine() {
        return new CasesPipeLine(updater);
    }
}
