package com.github.courtandrey.sudrfscraper.web.dto;

public class Payload {
    private String command;
    private String destination;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
