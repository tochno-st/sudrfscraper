package com.github.courtandrey.sudrfscraper.dump;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.courtandrey.sudrfscraper.dump.model.ConnectionType;
import com.github.courtandrey.sudrfscraper.dump.model.LinkType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static com.github.courtandrey.sudrfscraper.service.Constant.CSV_HEADER;

public class CSVConverterService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final String jsonFile;
    private final String csvFile;

    public CSVConverterService(String jsonFile, String csvFile) {
        this.jsonFile = jsonFile;
        this.csvFile = csvFile;
    }

    public void convertJsonToCSV() throws IOException {

        Path csvPath = Paths.get(csvFile);

        if (!Files.exists(csvPath)) {
            Files.createFile(csvPath);
        }

        Files.writeString(csvPath, CSV_HEADER + "\n");

        try (BufferedReader reader = new BufferedReader(new FileReader(jsonFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                JsonNode node = mapper.readTree(line);

                String csvLine = String.join(";", Arrays.asList(
                        node.get("id").asText(),
                        node.get("region").asText(),
                        node.get("instance").asText(),
                        node.get("name").asText(),
                        node.get("caseNumber").asText().replaceAll(";", ","),
                        node.get("entryDate").asText(),
                        node.get("names").asText("-").replaceAll(";", ","),
                        node.get("judge").asText("-").replaceAll(";", ","),
                        node.get("resultDate").asText("-"),
                        node.get("decision").asText("-").replaceAll(";", ","),
                        node.get("endDate").asText("-"),
                        node.get("consideredBy").asText("-"),
                        node.get("cui").asText("-"),
                        node.get("links").path(LinkType.META.toString()).asText("-"),
                        node.get("links").path(LinkType.TEXT.toString()).asText("-"),
                        node.get("links").path(LinkType.TEXT_AND_META.toString()).asText("-"),
                        node.get("connectedLinks").path(ConnectionType.LINK_UPPER.toString()).asText("-"),
                        node.get("connectedLinks").path(ConnectionType.LINK_LOWER.toString()).asText("-"),
                        node.get("connectedLinks").path(ConnectionType.CASE_NUMBER_UPPER.toString()).asText("-"),
                        node.get("connectedLinks").path(ConnectionType.CASE_NUMBER_LOWER.toString()).asText("-"),
                        node.get("text").asText("-")
                                .replaceAll(";", " ")
                                .replaceAll("\n", " ")));

                Files.writeString(csvPath, csvLine + "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            }
        }
    }
}
