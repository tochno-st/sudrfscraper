package com.github.courtandrey.sudrfscraper.web.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.article.Article;
import lombok.Getter;
import lombok.Setter;

@Getter
@JsonAutoDetect
public class RequestDetails {
    @Getter
    @JsonAutoDetect
    public static class Meta {
        private boolean needToContinue;

        public void setNeedToContinue(boolean needToContinue) {
            this.needToContinue = needToContinue;
        }

        private String name;
        private String chosenDirectory;
        private String[] levels;
        private String[] regions;
        private String filterMode;

        public void setFilterMode(String filterMode) {
            this.filterMode = filterMode;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setLevels(String[] levels) {
            this.levels = levels;
        }

        public void setRegions(String[] regions) {
            this.regions = regions;
        }

        public void setChosenDirectory(String chosenDirectory) {
            this.chosenDirectory = chosenDirectory;
        }
    }
    private String chosenDump;

    public void setChosenDump(String chosenDump) {
        this.chosenDump = chosenDump;
    }

    private Meta meta;
    private Article article;
    private String field;
    private ServerConnectionDetails connectionInf;
    private String endDate;
    private String startDate;
    @Setter
    private String entryEndDate;
    @Setter
    private String entryStartDate;
    private String text;
    private String[] instances;

    public void setInstances(String[] instances) {
        this.instances = instances;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setConnectionInf(ServerConnectionDetails connectionInf) {
        this.connectionInf = connectionInf;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setText(String text) {
        this.text = text;
    }
}
