package com.github.courtandrey.sudrfscraper.web.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.article.Article;

@JsonAutoDetect
public class RequestDetails {
    @JsonAutoDetect
    public static class Meta {
        private boolean needToContinue;

        public boolean isNeedToContinue() {
            return needToContinue;
        }

        public void setNeedToContinue(boolean needToContinue) {
            this.needToContinue = needToContinue;
        }

        private String name;
        private String chosenDirectory;
        private String[] levels;
        private String[] regions;
        private String filterMode;

        public String getFilterMode() {
            return filterMode;
        }

        public void setFilterMode(String filterMode) {
            this.filterMode = filterMode;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String[] getLevels() {
            return levels;
        }

        public void setLevels(String[] levels) {
            this.levels = levels;
        }

        public String[] getRegions() {
            return regions;
        }

        public void setRegions(String[] regions) {
            this.regions = regions;
        }

        public String getChosenDirectory() {
            return chosenDirectory;
        }

        public void setChosenDirectory(String chosenDirectory) {
            this.chosenDirectory = chosenDirectory;
        }
    }
    private String chosenDump;
    public String getChosenDump() {
        return chosenDump;
    }
    public void setChosenDump(String chosenDump) {
        this.chosenDump = chosenDump;
    }

    private Meta meta;
    private Article article;
    private String field;
    private ServerConnectionDetails connectionInf;
    private String endDate;
    private String startDate;
    private String text;

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public ServerConnectionDetails getConnectionInf() {
        return connectionInf;
    }

    public void setConnectionInf(ServerConnectionDetails connectionInf) {
        this.connectionInf = connectionInf;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
