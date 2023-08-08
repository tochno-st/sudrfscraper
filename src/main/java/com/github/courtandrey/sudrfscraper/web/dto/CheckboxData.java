package com.github.courtandrey.sudrfscraper.web.dto;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;

@JsonAutoDetect
@Getter
public class CheckboxData {

    private final boolean hidePage;

    public CheckboxData(boolean hidePage) {
        this.hidePage = hidePage;
    }
}
