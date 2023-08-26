package com.github.courtandrey.sudrfscraper.configuration.searchrequest.article;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Instance;

@JsonDeserialize(using = ArticleDeserializer.class)
public interface Article {
    Field getField();
    @JsonIgnore
    String getMainPart();
    @JsonIgnore
    boolean isEmpty();

    @JsonIgnore
    Instance[] getInstances();
}
