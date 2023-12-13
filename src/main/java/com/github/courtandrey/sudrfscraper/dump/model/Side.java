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
public class Side {
    private String sideType;
    private String sideName;
    private String INN;
    private String KPP;
    private String OGRN;
    private String OGRNIP;
    private Map<String, String> notParsed;
}
