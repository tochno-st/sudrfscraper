package com.github.courtandrey.sudrfscraper.service;

import com.github.courtandrey.sudrfscraper.configuration.ApplicationConfiguration;
import com.github.courtandrey.sudrfscraper.service.logger.Message;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ConstantsGetter {
    private static final Properties constantsProperties = new Properties();
    private static final Properties messagesProperties = new Properties();

    private static void init() {
        try {
            constantsProperties.load(new FileReader("./src/main/resources/constants/constants.properties"));
            messagesProperties.load(new FileReader("./src/main/resources/constants/messages.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static String getMessage(Message message) {
        if (messagesProperties.size() == 0) {
            init();
        }
        return messagesProperties.getProperty(message.name());
    }

    public synchronized static String getStringConstant(Constant constant) {
        if (constantsProperties.size() == 0) {
            init();
        }

        if (constant == Constant.BASIC_RESULT_PATH) {
            return ApplicationConfiguration.getInstance().getProperty("basic.result.path");
        }

        StringBuilder value = new StringBuilder(constantsProperties.getProperty(constant.name()));

        if (value.toString().contains(" + ")) {
            String[] splits= value.toString().split(" \\+ ");
            value = new StringBuilder();
            for (String split : splits) {
                try {
                    value.append(getStringConstant(Constant.valueOf(split)));
                } catch (IllegalArgumentException e) {
                    value.append(split);
                }
            }
        }
        return value.toString();
    }
}
