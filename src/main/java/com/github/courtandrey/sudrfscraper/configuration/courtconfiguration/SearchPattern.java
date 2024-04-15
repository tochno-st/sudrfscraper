package com.github.courtandrey.sudrfscraper.configuration.courtconfiguration;

import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Instance;
import com.github.courtandrey.sudrfscraper.exception.InitializationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static com.github.courtandrey.sudrfscraper.service.Constant.*;

public enum SearchPattern {
    DEPRECATED_PRIMARY_PATTERN,
    PRIMARY_PATTERN,
    VNKOD_PATTERN,
    DEPRECATED_SECONDARY_PATTERN,
    SECONDARY_PATTERN,
    MOSGORSUD_PATTERN;

    private Properties getProps(String path) {
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(path));
        } catch (IOException e) {
            throw new InitializationException(e);
        }
        return properties;
    }

    public String[] getPattern(Field field, Instance i) {
        switch (i) {
            case FIRST -> {
                return getFirstPattern(field);
            }
            case APPELLATION -> {
                return getAppellationPattern(field);
            }
            case CASSATION -> {
                return getCassationPattern(field);
            }
        }
        return new String[]{};
    }

    private String[] getCassationPattern(Field field) {
        switch (field) {
            case ADMIN -> {
                return getAdminPatternCassation();
            }
            case CRIMINAL -> {
                return getCriminalPatternCassation();
            }
            case CAS -> {
                return getCASPatternCassation();
            }
            case MATERIAL_PROCEEDING -> {
                return getMaterialProceedingPatternCassation();
            }
            case CIVIL -> {
                return getCivilPatternCassation();
            }
        }
        return new String[]{};
    }

    private String[] getCivilPatternCassation() {
        return String.valueOf(getProps(PATH_TO_CIVIL_PROPERTIES.toString()).getProperty(this + "_CASSATION")).split("\\$DELIMITER");
    }

    private String[] getMaterialProceedingPatternCassation() {
        return String.valueOf(getProps(PATH_TO_MATERIAL_PROCEEDING_PROPERTIES.toString()).getProperty(this + "_CASSATION")).split("\\$DELIMITER");
    }

    private String[] getCASPatternCassation() {
        return String.valueOf(getProps(PATH_TO_CAS_PROPERTIES.toString()).getProperty(this + "_CASSATION")).split("\\$DELIMITER");
    }

    private String[] getCriminalPatternCassation() {
        return String.valueOf(getProps(PATH_TO_CRIMINAL_PROPERTIES.toString()).getProperty(this + "_CASSATION")).split("\\$DELIMITER");
    }

    private String[] getAdminPatternCassation() {
        return String.valueOf(getProps(PATH_TO_ADMIN_PROPERTIES.toString()).getProperty(this + "_CASSATION")).split("\\$DELIMITER");
    }

    private String[] getAppellationPattern(Field field) {
        switch (field) {
            case ADMIN -> {
                return getAdminPatternAppellation();
            }
            case CRIMINAL -> {
                return getCriminalPatternAppellation();
            }
            case CAS -> {
                return getCASPatternAppellation();
            }
            case MATERIAL_PROCEEDING -> {
                return getMaterialProceedingPatternAppellation();
            }
            case CIVIL -> {
                return getCivilPatternAppellation();
            }
        }
        return new String[]{};
    }

    private String[] getCivilPatternAppellation() {
        return String.valueOf(getProps(PATH_TO_CIVIL_PROPERTIES.toString()).getProperty(this +"_APPELLATION")).split("\\$DELIMITER");
    }

    private String[] getMaterialProceedingPatternAppellation() {
        return String.valueOf(getProps(PATH_TO_MATERIAL_PROCEEDING_PROPERTIES.toString()).getProperty(this +"_APPELLATION")).split("\\$DELIMITER");
    }

    private String[] getCASPatternAppellation() {
        return String.valueOf(getProps(PATH_TO_CAS_PROPERTIES.toString()).getProperty(this +"_APPELLATION")).split("\\$DELIMITER");
    }

    private String[] getCriminalPatternAppellation() {
        return String.valueOf(getProps(PATH_TO_CRIMINAL_PROPERTIES.toString()).getProperty(this +"_APPELLATION")).split("\\$DELIMITER");
    }

    private String[] getAdminPatternAppellation() {
        return String.valueOf(getProps(PATH_TO_ADMIN_PROPERTIES.toString()).getProperty(this +"_APPELLATION")).split("\\$DELIMITER");
    }

    private String[] getFirstPattern(Field field) {
        switch (field) {
            case ADMIN -> {
                return getAdminPattern();
            }
            case CRIMINAL -> {
                return getCriminalPattern();
            }
            case CAS -> {
                return getCASPattern();
            }
            case MATERIAL_PROCEEDING -> {
                return getMaterialProceedingPattern();
            }
            case CIVIL -> {
                return getCivilPattern();
            }
        }
        return new String[]{};
    }

    private String[] getMaterialProceedingPattern() {
        return String.valueOf(getProps(PATH_TO_MATERIAL_PROCEEDING_PROPERTIES.toString()).getProperty(this.toString())).split("\\$DELIMITER");
    }

    public String[] getCASPattern() {
        return String.valueOf(getProps(PATH_TO_CAS_PROPERTIES.toString()).getProperty(this.toString())).split("\\$DELIMITER");
    }

    public String[] getCriminalPattern() {
        return String.valueOf(getProps(PATH_TO_CRIMINAL_PROPERTIES.toString()).getProperty(this.toString())).split("\\$DELIMITER");
    }

    public String[] getAdminPattern() {
        return String.valueOf(getProps(PATH_TO_ADMIN_PROPERTIES.toString()).getProperty(this.toString())).split("\\$DELIMITER");
    }

    public String[] getCivilPattern() {
        return String.valueOf(getProps(PATH_TO_CIVIL_PROPERTIES.toString()).getProperty(this.toString())).split("\\$DELIMITER");
    }
}















