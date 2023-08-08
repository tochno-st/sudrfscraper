package com.github.courtandrey.sudrfscraper.web.dto;

import lombok.Getter;

@Getter
public class Payload {
    private final String command;
    private final String destination;

    public Payload(String command, String destination) {
        this.command = command;
        this.destination = destination;
    }
}
