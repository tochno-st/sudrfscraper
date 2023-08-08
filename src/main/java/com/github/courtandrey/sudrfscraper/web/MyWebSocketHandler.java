package com.github.courtandrey.sudrfscraper.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.courtandrey.sudrfscraper.service.logger.LoggingLevel;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;
import com.github.courtandrey.sudrfscraper.web.dto.Payload;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Getter
public class MyWebSocketHandler extends TextWebSocketHandler {
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public MyWebSocketHandler() {
    }

    @Override
    @SuppressWarnings("all")
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            Payload payload = mapper.readValue(message.getPayload(),Payload.class);
            session.getAttributes().put("topic",payload.getDestination().substring(1));
            session.sendMessage(new TextMessage("{\"handshake_completed\":\"true\"}"));
        } catch (IOException e) {
            SimpleLogger.log(LoggingLevel.ERROR, Message.IOEXCEPTION_OCCURRED.toString() + e);
        }
    }
}
