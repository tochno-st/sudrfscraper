package com.github.courtandrey.sudrfscraper.configuration.searchrequest;

public enum Instance {
    FIRST,
    APPELLATION,
    CASSATION;

    public static Instance parseInstance(String instance) {
        switch (instance.toLowerCase().trim()) {
            case "first" -> {
                return FIRST;
            }
            case "appellation" -> {
                return APPELLATION;
            }
            case "cassation" -> {
                return CASSATION;
            }
            default -> {
                return null;
            }
        }
    }
}
