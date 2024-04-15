package com.github.courtandrey.sudrfscraper.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.courtandrey.sudrfscraper.configuration.ApplicationConfiguration;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.Level;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Field;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Instance;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.SearchRequest;
import com.github.courtandrey.sudrfscraper.controller.Starter;
import com.github.courtandrey.sudrfscraper.dump.model.Dump;
import com.github.courtandrey.sudrfscraper.service.SeleniumHelper;
import com.github.courtandrey.sudrfscraper.service.Validator;
import com.github.courtandrey.sudrfscraper.web.dto.Captcha;
import com.github.courtandrey.sudrfscraper.web.dto.CheckboxData;
import com.github.courtandrey.sudrfscraper.web.dto.RequestDetails;
import com.github.courtandrey.sudrfscraper.web.dto.ServerConnectionDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@SuppressWarnings("unused")
public class AjaxRestController {
    @Autowired
    private Validator validator;
    @Autowired
    private Starter starter;
    @Autowired
    private ApplicationController applicationController;

    @Autowired
    private SocketController socketController;
    @PostMapping("/save-checkbox")
    public CheckboxData saveCheckbox(@RequestBody CheckboxData checkboxData) {
        boolean hidePage = checkboxData.hidePage();
        ApplicationConfiguration.getInstance().setProperty("user.hide_info",String.valueOf(hidePage));
        return new CheckboxData(Boolean.parseBoolean(ApplicationConfiguration.getInstance().getProperty("user.hide_info")));
    }

    @GetMapping("/get_connection_info")
    public ResponseEntity<ServerConnectionDetails> getConnectionInfo() {
        ServerConnectionDetails serverConnectionDetails = new ServerConnectionDetails();
        if (!ApplicationConfiguration.getInstance().getProperty("sql.url").isEmpty()) {
            serverConnectionDetails.setUrl(ApplicationConfiguration.getInstance().getProperty("sql.url"));
        } else return ResponseEntity.notFound().build();
        if (!ApplicationConfiguration.getInstance().getProperty("sql.usr").isEmpty()) {
            serverConnectionDetails.setUser(ApplicationConfiguration.getInstance().getProperty("sql.usr"));
        } else return ResponseEntity.notFound().build();
        if (!ApplicationConfiguration.getInstance().getProperty("sql.password").isEmpty()) {
            serverConnectionDetails.setPassword(ApplicationConfiguration.getInstance().getProperty("sql.password"));
        } else return ResponseEntity.notFound().build();
        serverConnectionDetails.setRemember(true);
        return ResponseEntity.ok(serverConnectionDetails);
    }
    @PostMapping("/check_connection")
    public Map<String,Object> checkConnection(@RequestBody ServerConnectionDetails serverConnectionDetails) {
        Map<String, Object> response = new HashMap<>();
        boolean isConnected = false;
        try {
            starter.setServerConnectionInfoAndTest(serverConnectionDetails.getUrl(),
                    serverConnectionDetails.getUser(),
                    serverConnectionDetails.getPassword());
            if (serverConnectionDetails.isRemember()) {
                ApplicationConfiguration.getInstance().setProperty("sql.url", serverConnectionDetails.getUrl());
                ApplicationConfiguration.getInstance().setProperty("sql.usr", serverConnectionDetails.getUser());
                ApplicationConfiguration.getInstance().setProperty("sql.password", serverConnectionDetails.getPassword());
            }
            isConnected = true;
        } catch (SQLException ignored) {}
        response.put("connected", isConnected);
        return response;
    }

    @PostMapping("/submit_captcha")
    public void submitCaptcha(@RequestBody Captcha captcha) {
        socketController.setCaptcha(captcha.captcha());
    }

    @PostMapping("/check_request")
    public ResponseEntity<String> checkRequest(@RequestBody RequestDetails requestDetails,
    @RequestHeader(name = "Selected-Language") String language) {
        try {
            ResponseEntity<String> valid = validate(requestDetails);
            if (valid != null) return valid;

            if (!requestDetails.getArticle().isEmpty()) {
                SearchRequest.getInstance().setArticle(requestDetails.getArticle());
            } else if (requestDetails.getEndDate().isEmpty()) {
                SearchRequest.getInstance().setResultDateTill(LocalDate.now());
            }

            SearchRequest.getInstance().setField(Field.parseField(requestDetails.getField()));

            SearchRequest.getInstance().setInstanceList(
                    Arrays.stream(requestDetails.getInstances()).map(Instance::parseInstance).toArray(Instance[]::new)
            );

            if (!requestDetails.getMeta().getChosenDirectory().isEmpty()) {
                ApplicationConfiguration.getInstance().setProperty("basic.result.path", requestDetails.getMeta().getChosenDirectory());
            } else {
                ApplicationConfiguration.getInstance().setProperty("basic.result.path","./results/");
            }
            if (requestDetails.getMeta().getLevels().length > 0) {
                ApplicationConfiguration.getInstance().setProperty("basic.levels", arrayToString(requestDetails.getMeta().getLevels()));
            } else {
                ApplicationConfiguration.getInstance().setProperty("basic.levels", "");
            }
            if (requestDetails.getMeta().getRegions().length > 0) {
                ApplicationConfiguration.getInstance().setProperty("basic.regions", arrayToString(requestDetails.getMeta().getRegions()));
            } else {
                ApplicationConfiguration.getInstance().setProperty("basic.regions", "");
            }
            if (!requestDetails.getEndDate().isEmpty()) {
                SearchRequest.getInstance().setResultDateTill(validator.validateDate(requestDetails.getEndDate()));
            }
            if (!requestDetails.getStartDate().isEmpty()) {
                SearchRequest.getInstance().setResultDateFrom(validator.validateDate(requestDetails.getStartDate()));
            }
            if (!requestDetails.getEntryEndDate().isEmpty()) {
                SearchRequest.getInstance().setEntryDateTill(validator.validateDate(requestDetails.getEntryEndDate()));
            }
            if (!requestDetails.getEntryStartDate().isEmpty()) {
                SearchRequest.getInstance().setEntryDateFrom(validator.validateDate(requestDetails.getEntryStartDate()));
            }
            if (!requestDetails.getText().trim().isEmpty()) {
                SearchRequest.getInstance().setText(requestDetails.getText());
            }
            ApplicationConfiguration.getInstance().setProperty("cases.article_filter",String.valueOf(requestDetails.getMeta().getFilterMode()));
            ApplicationConfiguration.getInstance().setProperty("basic.continue",String.valueOf(requestDetails.getMeta().isNeedToContinue()));
            ApplicationConfiguration.getInstance().setProperty("basic.name",requestDetails.getMeta().getName());
            ApplicationConfiguration.getInstance().setProperty("basic.dump",requestDetails.getChosenDump());
            ApplicationConfiguration.getInstance().setProperty("basic.language", language);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal Error. Please change you request." +
                    " If it continues, write to sudarkinandrew@gmail.com");
        }
        applicationController.setScrapingProceeds(true);
        return ResponseEntity.ok("");
    }

    private String arrayToString(String[] arr) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            stringBuilder.append(arr[i]);
            if (i != arr.length - 1)
                stringBuilder.append(",");
        }
        return stringBuilder.toString();
    }

    private ResponseEntity<String> validate(RequestDetails requestDetails) {
        if (requestDetails.getMeta().getName().isEmpty()) return ResponseEntity.badRequest().body("Empty project name");
        if (requestDetails.getArticle() == null) {
            return ResponseEntity.badRequest().body("Wrong article format");
        }

        if (!requestDetails.getStartDate().isEmpty()) {
            if (validator.validateDate(requestDetails.getStartDate()) == null) {
                return ResponseEntity.badRequest().body("Wrong date format");
            }
        }

        if (!requestDetails.getEndDate().isEmpty()) {
            if (validator.validateDate(requestDetails.getEndDate()) == null) {
                return ResponseEntity.badRequest().body("Wrong date format");
            }
        }

        if (!requestDetails.getMeta().getChosenDirectory().isEmpty()) {
            if (!validator.validateDirectory(requestDetails.getMeta().getChosenDirectory())) {
                return ResponseEntity.badRequest().body("Invalid directory");
            }
        }
        if (requestDetails.getChosenDump().equals("SQL") &&
                !validator.validateName(requestDetails.getMeta().getName())) {
            return ResponseEntity.badRequest().body("Invalid name for SQL table");
        }
        return null;
    }

    @PostMapping("/fetch_previous_request")
    public ResponseEntity<RequestDetails> fetchPreviousRequest(@RequestBody String directoryPath) {
        Path requestFilePath = Path.of(directoryPath + "/request_details.json");
        try {
            ObjectMapper mapper = new ObjectMapper();
            String requestData = Files.readString(requestFilePath);
            RequestDetails requestDetail = mapper.readValue(requestData, RequestDetails.class);
            return ResponseEntity.ok(requestDetail);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    private boolean isFinishing = false;
    @GetMapping("/finish")
    public void finish() {
        if (isFinishing) return;
        isFinishing = true;
        try {
            starter.end();
            SeleniumHelper.killApp();
        } catch (Exception e) {
            System.out.println("Exception occurred while proceeding finishing routine");
        }
        System.exit(0);
    }

    @GetMapping("/init")
    public void init() {
        if (starter.isStarted()) return;
        starter.prepareScrapper(
                ApplicationConfiguration.getInstance().getProperty("basic.name"),
                Dump.parseDump(ApplicationConfiguration.getInstance().getProperty("basic.dump")));
        (new Thread(() -> starter.executeScrapping(Boolean.parseBoolean(ApplicationConfiguration.
                getInstance().getProperty("basic.continue"))))).start();
    }
}
