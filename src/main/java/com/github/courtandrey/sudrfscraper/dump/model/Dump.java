package com.github.courtandrey.sudrfscraper.dump.model;

public enum Dump {
    SQL,
    JSON;

    public static Dump parseDump(String dump) {
        for (Dump d: Dump.values()) {
            if (d.name().equals(dump)) {
                return d;
            }
        }
        throw new UnsupportedOperationException("Unknown dump type");
    }
}
