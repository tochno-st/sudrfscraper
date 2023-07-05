package com.github.courtandrey.sudrfscraper.configuration.searchrequest.article;

import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field;

public class MaterialProceedingArticle implements Article{

    private String partOfUPK;
    private String mosgorsudCode;

    public MaterialProceedingArticle() {
    }

    public MaterialProceedingArticle(String partOfUPK, String mosgorsudCode) {
        this.partOfUPK = partOfUPK;
        this.mosgorsudCode = mosgorsudCode;
    }

    public String getMosgorsudCode() {
        return mosgorsudCode;
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
}
