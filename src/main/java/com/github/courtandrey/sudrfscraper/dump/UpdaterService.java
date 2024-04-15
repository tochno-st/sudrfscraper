package com.github.courtandrey.sudrfscraper.dump;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.courtandrey.sudrfscraper.configuration.ApplicationConfiguration;
import com.github.courtandrey.sudrfscraper.configuration.dumpconfiguration.ServerConnectionInfo;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Instance;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.SearchRequest;
import com.github.courtandrey.sudrfscraper.controller.ErrorHandler;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.github.courtandrey.sudrfscraper.dump.model.Case;
import com.github.courtandrey.sudrfscraper.service.CasesPipeLineFactory;
import com.github.courtandrey.sudrfscraper.service.logger.LoggingLevel;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;
import com.github.courtandrey.sudrfscraper.web.dto.RequestDetails;
import com.github.courtandrey.sudrfscraper.web.dto.ServerConnectionDetails;

import static com.github.courtandrey.sudrfscraper.service.Constant.*;

public abstract class UpdaterService extends Thread implements Updater{

    protected boolean isScrappingOver;
    protected String dumpName;
    protected volatile Queue<Case> cases = new ArrayDeque<>();
    private final String SUMMARY;
    protected ErrorHandler handler;
    protected boolean isMetaNeeded = false;

    @Override
    public void startService() {
        CasesPipeLineFactory.init(this);
        this.start();
    }

    public UpdaterService(String dumpName, ErrorHandler handler) throws IOException{
        this.dumpName = dumpName;
        this.handler = handler;
        SUMMARY = String.format(PATH_TO_SUMMARY.toString(), dumpName, dumpName);
        Path dumpDirectory = Paths.get(BASIC_RESULT_PATH.toString());
        if (Files.notExists(dumpDirectory)) {
            Files.createDirectory(dumpDirectory);
        }
        Path dump = Paths.get(String.format(PATH_TO_RESULT_DIRECTORY.toString(), dumpName));
        if (Files.notExists(dump)) {
            Files.createDirectory(dump);
        }
    }

    @Override
    public void registerEnding() {
        isScrappingOver = true;
    }

    @Override
    public void addMeta() {
        isMetaNeeded = true;
    }

    protected abstract void createMeta() throws IOException;

    @Override
    public void joinService() throws InterruptedException {
        registerEnding();
        this.join();
    }

    protected HashMap<String, String> getBasicProperties() {
        HashMap<String, String> properties = new HashMap<>();
        SearchRequest sc = SearchRequest.getInstance();
        if (sc.getArticle() != null) {
            properties.put("article",sc.getArticle().toString());
        } else {
            properties.put("field",sc.getField().toString());
        }
        if (sc.getResultDateFrom() != null) {
            properties.put("result_date_from",sc.getResultDateFrom());
        }
        if (sc.getResultDateTill() != null) {
            properties.put("result_date_till",sc.getResultDateTill());
        }
        if (sc.getEntryDateFrom() != null) {
            properties.put("entry_date_from",sc.getEntryDateFrom());
        }
        if (sc.getEntryDateTill() != null) {
            properties.put("entry_date_till",sc.getEntryDateTill());
        }
        if (sc.getText() != null) {
            properties.put("text",sc.getText());
        }
        return properties;
    }

    protected void writeMeta(HashMap<String,String> meta) throws IOException {
        FileWriter writer = new FileWriter(String.format(PATH_TO_RESULT_META.toString(), dumpName, dumpName), StandardCharsets.UTF_8, false);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(writer, meta);
    }
    protected void afterExecute() throws IOException {
        close();

        if (isMetaNeeded) {
            createMeta();
        }
    }
    protected abstract void close();

    @Override
    public void addPreviousRequest() {
        try (FileWriter w = new FileWriter(String.format(PATH_TO_RESULT_DIRECTORY.toString(),dumpName)  + "request_details.json", StandardCharsets.UTF_8)) {
            RequestDetails requestDetails = new RequestDetails();
            requestDetails.setArticle(SearchRequest.getInstance().getArticle());
            String chosenDump = "JSON";
            if (this instanceof DBUpdaterService) {
                chosenDump = "SQL";
                ServerConnectionDetails serverConnectionDetails = getServerConnectionDetails();
                requestDetails.setConnectionInf(serverConnectionDetails);
            }
            requestDetails.setChosenDump(chosenDump);
            requestDetails.setText(SearchRequest.getInstance().getText() != null ? SearchRequest.getInstance().getText() : "");
            requestDetails.setField(SearchRequest.getInstance().getField().toString());
            requestDetails.setEndDate(SearchRequest.getInstance().getResultDateTill() != null ? SearchRequest.getInstance().getResultDateTill() : "");
            requestDetails.setStartDate(SearchRequest.getInstance().getResultDateFrom() != null ? SearchRequest.getInstance().getResultDateFrom() : "");
            requestDetails.setEntryEndDate(SearchRequest.getInstance().getEntryDateTill() != null ? SearchRequest.getInstance().getEntryDateTill() : "");
            requestDetails.setEntryStartDate(SearchRequest.getInstance().getEntryDateFrom() != null ? SearchRequest.getInstance().getEntryDateFrom() : "");
            requestDetails.setMeta(new RequestDetails.Meta());
            requestDetails.getMeta().setLevels((ApplicationConfiguration.getInstance().getProperty("basic.levels") != null &&
                    !ApplicationConfiguration.getInstance().getProperty("basic.levels").isEmpty())  ?
                    ApplicationConfiguration.getInstance().getProperty("basic.levels").split(",") : null);
            requestDetails.getMeta().setName(dumpName);
            requestDetails.getMeta().setFilterMode(ApplicationConfiguration.getInstance().getProperty("cases.article_filter") != null ?
                    ApplicationConfiguration.getInstance().getProperty("cases.article_filter") : "soft");
            requestDetails.getMeta().setChosenDirectory(ApplicationConfiguration.getInstance().getProperty("basic.result.path") != null ?
                    ApplicationConfiguration.getInstance().getProperty("basic.result.path") : "./results/");
            requestDetails.getMeta().setRegions((ApplicationConfiguration.getInstance().getProperty("basic.regions") != null &&
                    !ApplicationConfiguration.getInstance().getProperty("basic.regions").isEmpty()) ?
                    ApplicationConfiguration.getInstance().getProperty("basic.regions").split(",") : null);
            requestDetails.getMeta().setNeedToContinue(ApplicationConfiguration.getInstance().getProperty("basic.continue") != null &&
                    !ApplicationConfiguration.getInstance().getProperty("basic.continue").isEmpty()
                    && Boolean.parseBoolean(ApplicationConfiguration.getInstance().getProperty("basic.continue")));

            requestDetails.setInstances(Arrays.stream(SearchRequest.getInstance().getInstanceList()).map(Instance::name).toArray(String[]::new));

            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(w,requestDetails);
        } catch (IOException e) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.IOEXCEPTION_OCCURRED + " while trying to add previous request");
        }
    }

    private static ServerConnectionDetails getServerConnectionDetails() {
        ServerConnectionDetails serverConnectionDetails = new ServerConnectionDetails();
        serverConnectionDetails.setPassword(ServerConnectionInfo.getInstance().getPassword()!=null?ServerConnectionInfo.getInstance().getPassword() : "");
        serverConnectionDetails.setUrl(ServerConnectionInfo.getInstance().getDbUrl() != null ? ServerConnectionInfo.getInstance().getDbUrl() : "");
        serverConnectionDetails.setUser(serverConnectionDetails.getUser() != null ? ServerConnectionInfo.getInstance().getUser() : "");
        return serverConnectionDetails;
    }

    @Override
    public synchronized void update(Collection<Case> casesList) {
        cases.addAll(casesList);
    }

    @Override
    public void writeSummery(String text){
        try (FileWriter w = new FileWriter(SUMMARY, StandardCharsets.UTF_8)) {
            w.write(text);
            w.write(getSummeryInfo());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getSummeryInfo() throws IOException {
        StringBuilder returnString = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(PATH_TO_SUMMARY_INFO.toString()))) {
            while (reader.ready()) {
                returnString.append(reader.readLine());
                returnString.append("\n");
            }
        }
        return returnString.toString();
    }

}
