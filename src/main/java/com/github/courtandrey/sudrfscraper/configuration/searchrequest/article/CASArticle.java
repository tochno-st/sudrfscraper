package com.github.courtandrey.sudrfscraper.configuration.searchrequest.article;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field;
import lombok.Getter;

@Getter
@JsonAutoDetect
public class CASArticle implements Article{
    private String partOfCas;
    private String mosgorsudCode;

    public CASArticle() {
    }

    public String getMosgorsudCode() {
        return mosgorsudCode;
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
    public String toString() {
        return "Административное статья, включающая в себя " + partOfCas;
    }
}
