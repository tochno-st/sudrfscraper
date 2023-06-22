package com.github.courtandrey.sudrfscraper.dump;

import com.github.courtandrey.sudrfscraper.dump.model.Case;

import java.util.Collection;

public interface Updater {
    void startService();
    void update(Collection<Case> cases);
    void writeSummery(String summeryText);
    void joinService() throws InterruptedException;
    void registerEnding();
    void addMeta();
    void addPreviousRequest();
}
