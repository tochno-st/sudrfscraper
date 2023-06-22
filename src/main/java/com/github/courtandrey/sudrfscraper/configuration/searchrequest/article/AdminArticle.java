package com.github.courtandrey.sudrfscraper.configuration.searchrequest.article;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private boolean isSubArticlePresent = false;

    public AdminArticle() {
    }

    public int getChapter() {
        return chapter;
    }

    public void setChapter(int chapter) {
        this.chapter = chapter;
    }

    public int getArticle() {
        return article;
    }

    public void setArticle(int article) {
        this.article = article;
    }

    public int getSubArticle() {
        return subArticle;
    }

    public void setSubArticle(int subArticle) {
        this.subArticle = subArticle;
    }

    public int getPart() {
        return part;
    }

    public void setPart(int part) {
        this.part = part;
    }

    public int getSubPart() {
        return subPart;
    }

    public void setSubPart(int subPart) {
        this.subPart = subPart;
    }

    public AdminArticle(int chapter, int article, int subArticle, int part, int subPart) {
        this.chapter = chapter;
        this.article = article;
        this.subArticle = subArticle;
        this.part = part;
        this.subPart = subPart;
    }

    public AdminArticle(int chapter, int article, int partOrSubArticle, int partOrSubPart , boolean isSubArticlePresent) {
        this.chapter = chapter;
        this.article = article;
        this.isSubArticlePresent = isSubArticlePresent;
        if (isSubArticlePresent) {
            this.subArticle = partOrSubArticle;
            this.part = partOrSubPart;
        } else {
            this.part = partOrSubArticle;
            this.subPart = partOrSubPart;
        }
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

    public AdminArticle(int chapter, int article) {
        this.chapter = chapter;
        this.article = article;
    }

    public AdminArticle(int chapter, int article, int partOrSubArticle, boolean isSubArticlePresent) {
        this.chapter = chapter;
        this.article = article;
        this.isSubArticlePresent = isSubArticlePresent;
        if (isSubArticlePresent) {
            this.subArticle = partOrSubArticle;
        }
        else {
            this.part = partOrSubArticle;
        }
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
