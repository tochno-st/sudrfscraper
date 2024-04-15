package com.github.courtandrey.sudrfscraper.configuration.searchrequest;

import com.github.courtandrey.sudrfscraper.configuration.searchrequest.article.*;
import com.github.courtandrey.sudrfscraper.exception.InitializationException;
import com.github.courtandrey.sudrfscraper.exception.SearchRequestException;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

public class SearchRequest {
    private String resultDateFrom;
    private String resultDateTill;
    private String text;
    private Article article;
    private String publishedDateTill;
    @Setter
    @Getter
    private Instance[] instanceList;
    @Getter
    private String entryDateTill;
    @Getter
    private String entryDateFrom;

    public void setEntryDateTill(LocalDate entryDateTill) {
        this.entryDateTill = getDateToString(entryDateTill);
    }

    public void setEntryDateFrom(LocalDate entryDateFrom) {
        this.entryDateFrom = getDateToString(entryDateFrom);
    }

    @Getter
    private com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field field = com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field.CRIMINAL;

    private static SearchRequest instance;

    public synchronized String getPublishedDateTill() {
        return publishedDateTill;
    }

    public void setPublishedDateTill(LocalDate publishedDateTill) {
        this.publishedDateTill = getDateToString(publishedDateTill);
    }

    public void setStringPublishedDateTill(String dateTill) {
        this.publishedDateTill = dateTill;
    }

    public synchronized String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public synchronized Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        if (article instanceof CriminalArticle) setField(com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field.CRIMINAL);
        if (article instanceof AdminArticle) setField(com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field.ADMIN);
        if (article instanceof CASArticle) setField(com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field.CAS);
        if (article instanceof CivilArticle) setField(com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field.CIVIL);
        if (article instanceof MaterialProceedingArticle) setField(com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field.MATERIAL_PROCEEDING);
        this.article = article;
    }

    /**
     * @param date first day of period when the case ended.
     */
    public void setResultDateFrom(LocalDate date) {
        this.resultDateFrom = getDateToString(date);
    }

    public void setField(com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field field) {
        if (article != null && article.getField() != field) throw new SearchRequestException("Article does not match field");
        this.field=field;
    }

    private String getDateToString(LocalDate date) {
        int dom = date.getDayOfMonth();
        int m = date.getMonthValue();
        String day = dom>=10 ? String.valueOf(dom) : ("0"+dom);
        String month = m>=10 ? String.valueOf(m) : ("0"+m);
        return day+"."+month+"."+date.getYear();
    }
    public void setResultDateTill(LocalDate date) {
        this.resultDateTill = getDateToString(date);
    }

    private SearchRequest() {}

    public static SearchRequest getInstance() {
        if (instance==null) instance = new SearchRequest();
        return instance;
    }

    public boolean checkFields() {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field f:fields) {
            try {
                if (f.getName().equals("field") && f.get(this) == null) return false;
                if (!f.getName().equals("instance") && f.get(this) != null
                        && !f.getName().equals("field")) return true;
            } catch (IllegalAccessException e) {
                throw new InitializationException(e);
            }
        }
        setResultDateTill(LocalDate.now());
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        if (article != null) {
            builder.append("article = ").append(article).append(";");
        } else {
            builder.append("field = ").append(field).append(";");
        }
        if (resultDateFrom != null) {
            builder.append("resultDateFrom = ").append(resultDateFrom).append(";");
        }
        if (resultDateTill != null) {
            builder.append("resultDateTill = ").append(resultDateTill).append(";");
        }
        if (text != null) {
            builder.append("text = ").append(text).append(";");
        }
        builder.append("}");
        return builder.toString();
    }

    public synchronized String getResultDateFrom() {
        return resultDateFrom;
    }

    public synchronized String getResultDateTill() {
        return resultDateTill;
    }

}
