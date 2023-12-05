package com.github.courtandrey.sudrfscraper.dump.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Setter
@JsonAutoDetect
@NoArgsConstructor
@Getter
public class Step {
    private String eventName;
    private String eventDate;
    private String eventTime;
    private String eventPlace;
    private String eventResult;
    private String eventResultReason;
    private String additionalInfo;
    private String publishedDate;
    private Map<String, String> notParsed;
}
