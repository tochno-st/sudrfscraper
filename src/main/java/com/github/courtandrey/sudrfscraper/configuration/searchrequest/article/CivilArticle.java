package com.github.courtandrey.sudrfscraper.configuration.searchrequest.article;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonAutoDetect
public class CivilArticle implements CategorizedArticle {
    private String partOfCas;
    private String mosgorsudCode;

    @Override
    public Field getField() {
        return Field.CIVIL;
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
        return "Гражданское производство: " + partOfCas;
    }
}
