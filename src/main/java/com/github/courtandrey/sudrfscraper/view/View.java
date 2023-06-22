package com.github.courtandrey.sudrfscraper.view;

import com.github.courtandrey.sudrfscraper.controller.Starter;
import com.github.courtandrey.sudrfscraper.service.logger.Message;

import java.awt.image.BufferedImage;

public interface View {
    String showCaptcha(BufferedImage image) throws InterruptedException;
    void finish();
    void showFrameWithInfo(ViewFrame viewFrame, Message message, String... args);
}
