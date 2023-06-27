package com.github.courtandrey.sudrfscraper.configuration;

import com.github.courtandrey.sudrfscraper.exception.InitializationException;
import com.github.courtandrey.sudrfscraper.service.logger.LoggingLevel;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import static com.github.courtandrey.sudrfscraper.service.Constant.PATH_TO_APP_PROPERTIES;

public class ApplicationConfiguration {
    public static final Properties props = new Properties();
    private static String usrDir = System.getProperty("user.dir");

    private ApplicationConfiguration() {
        try {
            props.load(new FileReader(PATH_TO_APP_PROPERTIES.toString()));
        } catch (IOException e) {
            throw new InitializationException(e);
        }
    }

    private static ApplicationConfiguration configuration;

    public static ApplicationConfiguration getInstance() {
        if (configuration == null) {
            configuration = new ApplicationConfiguration();
        }
        return configuration;
    }

    public static String getUsrDir() {
        return usrDir;
    }

    public static void setUsrDir(String usrDir) {
        ApplicationConfiguration.usrDir = usrDir;
    }

    public synchronized String getProperty(String key) {
        return props.getProperty(key);
    }
    public synchronized void setProperty(String key, String value) {
        props.setProperty(key, value);
        try {
            props.store(new FileWriter(PATH_TO_APP_PROPERTIES.toString()),"");
        } catch (IOException e) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.IOEXCEPTION_OCCURRED);
        }
    }
}
