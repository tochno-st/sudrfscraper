package com.github.courtandrey.sudrfscraper.controller;

import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.CourtConfiguration;
import com.github.courtandrey.sudrfscraper.configuration.dumpconfiguration.ServerConnectionInfo;
import com.github.courtandrey.sudrfscraper.dump.model.Case;
import com.github.courtandrey.sudrfscraper.dump.model.Dump;
import com.github.courtandrey.sudrfscraper.service.ConfigurationLoader;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.courtandrey.sudrfscraper.service.Constant.PATH_TO_RESULT_JSON;
import static com.github.courtandrey.sudrfscraper.service.Constant.PATH_TO_RESULT_META;

@SuppressWarnings("unused")
public class ScrapingAnalyzer {
    private static String name;
    private ScrapingAnalyzer analyzer;

    private ScrapingAnalyzer(String name) {
        ScrapingAnalyzer.name =name;
    }

    public ScrapingAnalyzer(String name, Dump dump){
        this.analyzer = getAnalyzer(dump,name);
    }

    public void showCasesPerRegion() throws IOException {
        analyzer.showCasesPerRegion();
    }

    public void showRandomText() throws IOException {
        analyzer.showRandomText();
    }

    public void showTextNumber() throws IOException {
        analyzer.showTextNumber();
    }

    private ScrapingAnalyzer getAnalyzer(Dump dump, String name) {
        switch (dump) {
            case JSON -> {
                return new JSONScrapingAnalyzer(name);
            }
            case SQL -> {
                return new MySQLScrapingAnalyzer(name);
            }
        }
        throw new UnsupportedOperationException(Message.UNKNOWN_DUMP.toString());
    }

    static class JSONScrapingAnalyzer extends ScrapingAnalyzer {
        private final String PATH_TO_DUMP;

        public JSONScrapingAnalyzer(String name) {
            super(name);
            PATH_TO_DUMP = String.format(PATH_TO_RESULT_JSON.toString(), name, name);
        }
        @Override
        public void showCasesPerRegion() throws IOException {
            BufferedReader reader = Files.newBufferedReader(Paths.get(PATH_TO_DUMP));
            List<CourtConfiguration> ccs = ConfigurationLoader.getCourtConfigurations();
            HashMap<Integer,Integer> checkMap = new HashMap<>();

            for (CourtConfiguration cc:ccs) {
                checkMap.put(cc.getRegion(),0);
            }

            ObjectMapper mapper = new ObjectMapper();
            while (reader.ready()) {
                String str = reader.readLine();
                if (str.equals("")) continue;
                Case _case = mapper.readValue(str,Case.class);
                checkMap.merge(_case.getRegion(),1,Integer::sum);
            }

            checkMap.keySet()
                    .stream()
                    .sorted(Comparator.comparing(x -> checkMap.get((Integer) x)).reversed()).collect(Collectors.toList())
                    .forEach(x -> SimpleLogger.println(String.format(Message.CASES_PER_REGION.toString(), x, checkMap.get(x))));

            reader.close();
        }

        private int countStringsFromDump() throws IOException {
            int stringCount = 0;
            Path dumpPath = Paths.get(PATH_TO_DUMP);
            BufferedReader reader = Files.newBufferedReader(dumpPath);
            while (reader.ready()) {
                reader.readLine();
                stringCount = stringCount + 1;
            }
            reader.close();
            return stringCount;
        }

        @Override
        public void showRandomText() throws IOException {
            int stringCount;

            Path dumpPath = Paths.get(PATH_TO_DUMP);
            if (Files.notExists(dumpPath)) {
                stringCount = countStringsFromDump();
            }

            else {
                HashMap<String, String> meta = getMeta();
                if (meta == null) {
                    stringCount = countStringsFromDump();
                }
                else {
                    stringCount = Integer.parseInt(meta.get("string_count"));
                }
            }

            List<Case> cases = new ArrayList<>();

            BufferedReader reader = Files.newBufferedReader(dumpPath);
            ObjectMapper mapper = new ObjectMapper();

            for (int i = 0; i < stringCount; i++) {
                Case _case = mapper.readValue(reader.readLine(),Case.class);
                if (_case.getText() != null) {
                    cases.add(_case);
                }
                if (i == stringCount - 1 && cases.size() == 0) {
                    SimpleLogger.println(Message.NO_DECISION_TEXT);
                    return;
                }
            }

            reader.close();

            int textNum = (int) (Math.random() * cases.size());
            SimpleLogger.println(cases.get(textNum).getText());
        }

        @Override
        public void showTextNumber() throws IOException {
            BufferedReader reader = Files.newBufferedReader(Paths.get(PATH_TO_DUMP));
            ObjectMapper mapper = new ObjectMapper();
            Case _case;
            int textNum = 0;
            int stringNum = 0;
            while (reader.ready()) {
                String str = reader.readLine();
                if (str.equals("")) continue;
                _case = mapper.readValue(str, Case.class);
                stringNum = stringNum + 1;
                if (_case.getText() != null) textNum = textNum + 1;
            }
            reader.close();
            SimpleLogger.println(String.format(Message.CASES_WITH_TEXTS.toString(), stringNum, textNum));
        }

        @SuppressWarnings("unchecked")
        private HashMap<String,String> getMeta() {
            try {
                return (HashMap<String, String>) (new ObjectMapper()).readValue(new FileReader(
                                String.format(PATH_TO_RESULT_META.toString(), name, name)),
                        HashMap.class);
            } catch (Exception e) {
                return  null;
            }
        }
    }

    static class MySQLScrapingAnalyzer extends ScrapingAnalyzer {
        public MySQLScrapingAnalyzer(String name) {
            super(name);
        }

        @Override
        public void showCasesPerRegion() {

        }

        @Override
        public void showRandomText() {

        }

        @Override
        public void showTextNumber() {

        }
    }

    public void setServerConnectionInfo(String DB_URL, String user, String password) {
        if (!(analyzer instanceof MySQLScrapingAnalyzer)) throw new UnsupportedOperationException(Message.WRONG_DUMP.toString());
        ServerConnectionInfo info = ServerConnectionInfo.getInstance();
        info.setDbUrl(DB_URL);
        info.setUser(user);
        info.setPassword(password);
    }
}
