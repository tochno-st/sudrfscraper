package com.github.courtandrey.sudrfscraper.service;

import com.github.courtandrey.sudrfscraper.dump.Updater;
import com.github.courtandrey.sudrfscraper.dump.model.Case;

import java.util.Collection;

public class CasesPipeLine {
    private final Updater updater;

    public CasesPipeLine(Updater updater) {
        this.updater = updater;
    }

    public void offer(Collection<Case> _case) {
        updater.update(_case);
    }


}
