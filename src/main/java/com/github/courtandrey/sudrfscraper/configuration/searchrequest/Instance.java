package com.github.courtandrey.sudrfscraper.configuration.searchrequest;

public enum Instance {
    FIRST,
    APPELLATION;

    public static Instance parseInstance(String instance) {
        switch (instance.toLowerCase().trim()) {
            case "first" -> {
                return FIRST;
            }
            case "appellation" -> {
                return APPELLATION;
            }
            default -> {
                return null;
            }
        }
    }
}
