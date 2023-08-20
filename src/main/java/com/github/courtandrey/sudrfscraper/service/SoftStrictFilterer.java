package com.github.courtandrey.sudrfscraper.service;

import com.github.courtandrey.sudrfscraper.dump.model.Case;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class SoftStrictFilterer implements Predicate<Case> {
    private final String mainPart;
    private final SoftStrictMode softStrictMode;

    public SoftStrictFilterer(String mainPart, SoftStrictMode softStrictMode) {
        this.mainPart = mainPart;
        this.softStrictMode = softStrictMode;
    }

    @Override
    public boolean test(Case _case) {
        String reg2;
        if (softStrictMode == SoftStrictMode.STRICT_MODE) {
            reg2 = "[^\\d.](.*)";
        }
        else {
            reg2 = "\\D(.*)";
        }
        return _case.getNames() != null &&
                _case.getNames().matches("(.*)" + "\\D" + prepareForRegex(mainPart) + reg2);
    }

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

    private String prepareForRegex(String src) {
        src = src.replaceAll("\\(","\\\\(");
        src = src.replaceAll("\\)", "\\\\)");
        src = src.replaceAll("\\.", "\\\\.");
        return src;
    }
}
