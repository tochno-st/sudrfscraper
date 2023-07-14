package com.github.courtandrey.sudrfscraper.configuration.searchrequest.article;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field;
import lombok.Getter;

@Getter
@JsonAutoDetect
public class MaterialProceedingArticle implements Article, MosGorSudCategoryArticle{

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

    @Override
    public String toString() {
        return "Производство по материалам: " + partOfUPK;
    }
}
