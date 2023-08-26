package com.github.courtandrey.sudrfscraper.configuration.searchrequest.article;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Instance;
import lombok.Getter;

@Getter
@JsonAutoDetect
public class MaterialProceedingArticle implements CategorizedArticle {

    private String partOfUPK;
    private String mosgorsudCode;

    @JsonIgnore
    private final static Instance[] INSTANCES = {Instance.FIRST};



    public MaterialProceedingArticle() {
    }

    public MaterialProceedingArticle(String partOfUPK, String mosgorsudCode) {
        this.partOfUPK = partOfUPK;
        this.mosgorsudCode = mosgorsudCode;
    }


    @Override
    public Field getField() {
        return Field.MATERIAL_PROCEEDING;
    }

    @Override
    public String getMainPart() {
        return partOfUPK;
    }

    @Override
    public boolean isEmpty() {
        return partOfUPK == null && mosgorsudCode == null;
    }

    @Override
    public Instance[] getInstances() {
        return INSTANCES;
    }

    @Override
    public String toString() {
        return "Производство по материалам: " + partOfUPK;
    }
}
