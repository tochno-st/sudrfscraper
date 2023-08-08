package com.github.courtandrey.sudrfscraper.web.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

@JsonAutoDetect
@Getter
@Setter
public class ServerConnectionDetails {

    private String url;
    private String user;
    private String password;
    private boolean remember;
}
