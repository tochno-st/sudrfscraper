package com.github.courtandrey.sudrfscraper.configuration.searchrequest.article;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field;
import lombok.Getter;

@Getter
@JsonAutoDetect
public class CriminalArticle implements Article, SoftStrictFilterable{
    private int article = 0;
    private int part = 0;
    private char letter = 0;
    private int subArticle = 0;

    public CriminalArticle() {
    }

    public int getArticle() {
        return article;
    }

    public void setArticle(int article) {
        this.article = article;
    }

    public int getPart() {
        return part;
    }

    public void setPart(int part) {
        this.part = part;
    }

    public char getLetter() {
        return letter;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }

    public int getSubArticle() {
        return subArticle;
    }

    public void setSubArticle(int subArticle) {
        this.subArticle = subArticle;
    }

    public CriminalArticle(int article, int partOrSubArticle, boolean isSubArticlePresent) {
        this.article = article;
        if (isSubArticlePresent) {
            subArticle = partOrSubArticle;
        } else {
            part = partOrSubArticle;
        }
    }

    public CriminalArticle(int article, int subArticle, int part) {
        this.article = article;
        this.part = part;
        this.subArticle = subArticle;
    }

    public CriminalArticle(int article, int subArticle, int part, char letter) {
        this.article = article;
        this.part = part;
        this.letter = letter;
        this.subArticle = subArticle;
    }

    public CriminalArticle(int article, int part, char letter) {
        this.article = article;
        this.part = part;
        this.letter = letter;
    }

    public CriminalArticle(int article) {
        this.article = article;
    }

    public CriminalArticle(int article, int part) {
        this.article = article;
        this.part = part;
    }

    @Override
    public String getMainPart() {
        return subArticle == 0 ? String.valueOf(article) : article + "." + subArticle;
    }

    @Override
    public boolean isEmpty() {
        return article == 0;
    }

    @Override
    public Field getField() {
        return Field.CRIMINAL;
    }

    @Override
    public String toString() {
        String returnString = "Уголовная Статья " + article;
        if (subArticle > 0) returnString += "." + subArticle;
        if (part > 0) returnString += " ч." + part;
        if (letter != 0) returnString += " п." + letter;
        return returnString;
    }
}
