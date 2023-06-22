package com.github.courtandrey.sudrfscraper.view;

import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.web.SocketController;
import com.github.courtandrey.sudrfscraper.web.dto.InfoType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.Arrays;

@Component
public class SocketView implements LogProccessingView {
    @Autowired
    private SocketController socketController;
    @Override
    public String showCaptcha(BufferedImage image) throws InterruptedException {
        return socketController.handleCaptcha(image);
    }

    @Override
    public void finish() {}

    @Override
    public void showFrameWithInfo(ViewFrame viewFrame, Message message, String... args) {
          if (viewFrame == ViewFrame.ERROR) {
              showError(message, args);
              return;
          }
          if (message == Message.RESULT) {
              updateResult(args);
              return;
          }
        if (viewFrame == ViewFrame.INFO) {
            showInfo(message, args);
            return;
        }
          throw new UnsupportedOperationException("Unknown message");
    }

    private void showInfo(Message message, String... args) {
        socketController.handleProgressInfoUpdate(InfoType.INFO, message.toString() + Arrays.toString(args)
                .replace("[","").replace("]",""));
    }

    private void updateResult(String... args) {
        socketController.handleProgressUpdate(Integer.parseInt(args[0]),Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }
    @Override
    public void addLog(String text) {
        socketController.handleLogsUpdate(text);
    }



    private void showError(Message message, String...args) {
        socketController.handleProgressInfoUpdate(InfoType.ERROR, message.toString() + Arrays.toString(args));
    }


}
