package com.github.courtandrey.sudrfscraper.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.courtandrey.sudrfscraper.service.ThreadHelper;
import com.github.courtandrey.sudrfscraper.service.logger.LoggingLevel;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;
import com.github.courtandrey.sudrfscraper.web.dto.InfoType;
import com.github.courtandrey.sudrfscraper.web.dto.ProgressData;
import com.github.courtandrey.sudrfscraper.web.dto.ProgressInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.AbstractWebSocketMessage;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller
public class SocketController {
    private final ObjectMapper mapper = new ObjectMapper();
    private final MyWebSocketHandler webSocketHandler;

    @Autowired
    public SocketController(MyWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }
    private String captcha;

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public String handleCaptcha(BufferedImage image) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            SimpleLogger.log(LoggingLevel.ERROR, Message.IOEXCEPTION_OCCURRED + e.getMessage());
        }

        byte[] imageBytes = outputStream.toByteArray();

        sendRegardingFilter("topic", "captcha",new BinaryMessage(imageBytes));

        while (true) {
            if (captcha == null) {
                ThreadHelper.sleep(3);
            } else {
                break;
            }
        }
        String ret = captcha;
        this.captcha = null;
        return  ret;
    }

    public void handleProgressUpdate(int current, int total, int scraped) {
            ProgressData progressData = new ProgressData(current, total, scraped);
            try {
                sendRegardingFilter("topic", "progress", new TextMessage(mapper.writeValueAsString(progressData)));
            } catch (IOException e) {
                SimpleLogger.log(LoggingLevel.ERROR, Message.IOEXCEPTION_OCCURRED + e.getMessage());
            }

    }

    public void handleProgressInfoUpdate(InfoType infoType, String text) {
        ProgressInfo progressInfo = new ProgressInfo(text, infoType);
        try {
            sendRegardingFilter("topic", "info", new TextMessage(mapper.writeValueAsString(progressInfo)));
        } catch (IOException e) {
            SimpleLogger.log(LoggingLevel.ERROR, Message.IOEXCEPTION_OCCURRED + e.getMessage());
        }
    }

    public void handleLogsUpdate(String text) {
        sendRegardingFilter("topic", "logs", new TextMessage(text));
    }

    private void sendRegardingFilter(String attr, String val, AbstractWebSocketMessage o) {
        for (WebSocketSession session : webSocketHandler.getSessions()) {
            try {
                if (!session.getAttributes().get(attr).equals(val) || !session.isOpen()) continue;
                session.sendMessage(o);
            } catch (IOException e) {
                SimpleLogger.log(LoggingLevel.ERROR, Message.IOEXCEPTION_OCCURRED + e.getMessage());
            }
        }
    }
}
