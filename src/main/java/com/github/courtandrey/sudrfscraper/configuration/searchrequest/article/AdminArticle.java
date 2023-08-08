package com.github.courtandrey.sudrfscraper.configuration.searchrequest.article;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field;
import lombok.Getter;

@Getter
@JsonAutoDetect
public class AdminArticle implements Article {
    private int chapter;
    private int article;
    private int subArticle;
    private int part;
    private int subPart;

    public AdminArticle() {
    }

    public void setChapter(int chapter) {
        this.chapter = chapter;
    }

    public void setArticle(int article) {
        this.article = article;
    }

    public void setSubArticle(int subArticle) {
        this.subArticle = subArticle;
    }

    public void setPart(int part) {
        this.part = part;
    }

    public void setSubPart(int subPart) {
        this.subPart = subPart;
    }
    @Override
    public String getMainPart() {
        String returnString = chapter+"." + article;
        if (subArticle != 0 ) returnString += "." + subArticle;
        return returnString;
    }

    @Override
    public boolean isEmpty() {
        return chapter == 0 && article == 0;
    }


    @Override
    public Field getField() {
        return Field.ADMIN;
    }

    @Override
    public String toString() {
        String returnString = "Статья КоАП " + chapter+"." + article;
        if (subArticle != 0 ) returnString += "." + subArticle;
        if (part != 0) returnString += " ч." + part;
        if (subPart != 0) returnString += "." + subPart;
        return returnString;
    }

}
