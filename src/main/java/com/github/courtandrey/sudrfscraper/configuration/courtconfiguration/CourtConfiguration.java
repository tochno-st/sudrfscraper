package com.github.courtandrey.sudrfscraper.configuration.courtconfiguration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.strategy.Connection;
import lombok.Data;

import java.util.HashMap;
import java.util.Objects;

@JsonAutoDetect
@Data
public class CourtConfiguration {
    private int id;
    private int region;
    private String searchString;
    private StrategyName strategyName;
    private Level level;
    private Issue issue;
    private HashMap<Field,String> workingUrl;
    private SearchPattern searchPattern;
    private String vnkod;
    private String name;
    private Connection connection;
    private boolean hasCaptcha;
    @JsonIgnore
    public void putWorkingUrl(Field field, String value) {
        workingUrl.put(field,value);
    }

    @JsonIgnore
    public boolean isSingleStrategy() {
        return connection == Connection.SELENIUM || strategyName == StrategyName.CAPTCHA_STRATEGY;
    }

    @Override
    public String toString() {
        return String.format(Message.COURT_CONFIG_TO_STRING.toString(),name,searchString);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourtConfiguration that = (CourtConfiguration) o;
        return Objects.equals(searchString, that.searchString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchString);
    }

}
