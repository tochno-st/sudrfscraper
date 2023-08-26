package com.github.courtandrey.sudrfscraper.configuration.searchrequest.article;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Instance;
import lombok.Getter;

@Getter
@JsonAutoDetect
public class CASArticle implements CategorizedArticle {
    private String partOfCas;
    private String mosgorsudCode;

    @JsonIgnore
    private final static Instance[] INSTANCES = {Instance.FIRST, Instance.APPELLATION};

    public CASArticle() {
    }

    public CASArticle(String partOfCas, String mosgorsudCode) {
        this.partOfCas = partOfCas;
        this.mosgorsudCode = mosgorsudCode;
    }

    @Override
    public Field getField() {
        return Field.CAS;
    }

    @Override
    public String getMainPart() {
        return partOfCas;
    }

    @Override
    public boolean isEmpty() {
        return partOfCas == null && mosgorsudCode == null;
    }

    @Override
    public Instance[] getInstances() {
        return INSTANCES;
    }

    @Override
    public String toString() {
        return "Административное производство: " + partOfCas;
    }
}
