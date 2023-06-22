package com.github.courtandrey.sudrfscraper.configuration.courtconfiguration;

import com.github.courtandrey.sudrfscraper.exception.StrategyParsingException;

public enum StrategyName {
    PRIMARY_STRATEGY,
    CAPTCHA_STRATEGY,
    END_STRATEGY,
    MOSGORSUD_STRATEGY;

    public static StrategyName parseStrategy(String s) {
        if (s.equalsIgnoreCase(PRIMARY_STRATEGY.name())) return PRIMARY_STRATEGY;
        if (s.equalsIgnoreCase(CAPTCHA_STRATEGY.name())) return CAPTCHA_STRATEGY;
        if (s.equalsIgnoreCase(END_STRATEGY.name())) return END_STRATEGY;
        throw new StrategyParsingException("Unable to parse Level");
    }
}
