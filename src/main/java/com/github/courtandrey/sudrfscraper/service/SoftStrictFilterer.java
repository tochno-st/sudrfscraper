package com.github.courtandrey.sudrfscraper.service;

import com.github.courtandrey.sudrfscraper.dump.model.Case;

import java.util.HashSet;
import java.util.Set;

public class SoftStrictFilterer {
    public enum SoftStrictMode {
        STRICT_MODE,
        SOFT_MODE;

        public static SoftStrictMode parseMode(String mode) {
            switch (mode.trim().toLowerCase()) {
                case "strict", "strict_mode", "strictmode" -> {
                    return STRICT_MODE;
                }

                case "soft", "soft_mode", "softmode" -> {
                    return SOFT_MODE;
                }

                default -> {
                    return null;
                }
            }
        }
    }
    public HashSet<Case> filter(Set<Case> src, String mainPart, SoftStrictMode softStrictMode) {
        HashSet<Case> cases = new HashSet<>();
        String reg2;
        if (softStrictMode == SoftStrictMode.STRICT_MODE) {
            reg2 = "[^\\d.](.*)";
        }
        else {
            reg2 = "\\D(.*)";
        }
        for (Case _case:src) {
            if (_case.getNames() != null && _case.getNames().matches("(.*)" +"\\D"+ prepareForRegex(mainPart) + reg2)) {
                cases.add(_case);
            }
        }
        return cases;
    }

    private String prepareForRegex(String src) {
        src = src.replaceAll("\\(","\\\\(");
        src = src.replaceAll("\\)", "\\\\)");
        src = src.replaceAll("\\.", "\\\\.");
        return src;
    }
}
