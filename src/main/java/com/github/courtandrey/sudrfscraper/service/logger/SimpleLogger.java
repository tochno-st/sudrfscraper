package com.github.courtandrey.sudrfscraper.service.logger;

import com.github.courtandrey.sudrfscraper.configuration.ApplicationConfiguration;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.CourtConfiguration;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.Issue;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.SearchRequest;
import com.github.courtandrey.sudrfscraper.view.LogProccessingView;
import com.github.courtandrey.sudrfscraper.view.View;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.github.courtandrey.sudrfscraper.service.Constant.PATH_TO_COURT_HISTORY;
import static com.github.courtandrey.sudrfscraper.service.Constant.PATH_TO_LOGS;

public final class SimpleLogger {
    private static FileWriter logWriter;
    private static final DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss");
    private static String name = "default";
    private static Boolean useCourtHistory;
    private static boolean isInited = false;
    private static LogProccessingView view = null;

    private SimpleLogger() {}
    public static void initLogger(String name) {
        if (!isInited) {
            initLogger(name, null);
            isInited = true;
        }
    }

    public static void initLogger(String name, View view) {
        if (view instanceof LogProccessingView) {
            SimpleLogger.view = (LogProccessingView) view;
        }
        SimpleLogger.name = name;
        useCourtHistory = Boolean.parseBoolean(ApplicationConfiguration.getInstance().getProperty("dev.log.court_history"));
        isInited = true;
    }

    public static void initLogger() {
        if (!isInited) {
            initLogger(name, null);
            isInited = true;
        }
    }

    public synchronized static void log(LoggingLevel level, Object message) {
        try {
            LocalDateTime ldt = LocalDateTime.now();
            String string = level.toString() + " " + ldt.format(dt) + " " + message;
            getLogWriter().write(string + "\n");
            getLogWriter().flush();
            System.out.print(string + "\n");
            if (view != null) view.addLog(string);
        } catch (IOException e) {
            reopen();
            System.out.println(message);
        }
    }

    public synchronized static void addToCourtHistory(CourtConfiguration cc, int numberOfCases) throws IOException {
        if (!useCourtHistory) return;
        initLogger();
        Path courtHistory = Paths.get("./src/main/resources/courts/");
        if (Files.notExists(courtHistory)) Files.createDirectory(courtHistory);
        try (FileWriter writer = new FileWriter(String.format(PATH_TO_COURT_HISTORY.toString(), cc.getId()), true)) {
            if (cc.getIssue() == null)  {
                cc.setIssue(Issue.ERROR);
            }
            writer.write(cc.getIssue().toString() + " " + numberOfCases + " " + LocalDate.now() + " " + SearchRequest.getInstance().toString());
            writer.write("\n");
        } catch (IOException e) {
            log(LoggingLevel.ERROR, String.format(Message.IOEXCEPTION_OCCURRED.toString(),e));
        }
    }

    private synchronized static void reopen() {
        if (logWriter != null) {
            try {
                logWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logWriter = null;
    }

    private static FileWriter getLogWriter() throws IOException {
        if (logWriter == null) {
            logWriter = new FileWriter(String.format(PATH_TO_LOGS.toString(), name, name),true);
            log(LoggingLevel.INFO, Message.BEGINNING_OF_EXECUTION);
        }
        return logWriter;
    }

    public synchronized static void println(Object message) {
        System.out.println(message);
    }

    public static void close() throws IOException{
        if (logWriter != null) {
            logWriter.flush();
            logWriter.close();
        }
    }
}
