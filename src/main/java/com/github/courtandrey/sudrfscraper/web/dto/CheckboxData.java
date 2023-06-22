package com.github.courtandrey.sudrfscraper.web.dto;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.article.Article;

@JsonAutoDetect
public class CheckboxData {

    private boolean hidePage;

    public boolean isHidePage() {
        return hidePage;
    }

    public void setHidePage(boolean hidePage) {
        this.hidePage = hidePage;
    }

}
