package com.github.courtandrey.sudrfscraper.dump;

import com.github.courtandrey.sudrfscraper.controller.ErrorHandler;
import com.github.courtandrey.sudrfscraper.dump.model.Case;
import com.github.courtandrey.sudrfscraper.service.ThreadHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.github.courtandrey.sudrfscraper.service.Constant.*;

public class JSONUpdaterService extends UpdaterService {

    private FileWriter fileWriter;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String fileName;
    private final String meta;
    private final CSVConverterService csvConverterService;

    public JSONUpdaterService(String dumpName, ErrorHandler handler) throws IOException {
        super(dumpName, handler);
        fileName = String.format(PATH_TO_RESULT_JSON.toString(), dumpName, dumpName);
        meta = String.format(PATH_TO_RESULT_META.toString(),dumpName,dumpName);
        csvConverterService = new CSVConverterService(fileName,
                String.format(PATH_TO_RESULT_CSV.toString(), dumpName, dumpName));
        try {
            renew();
            if (Files.size(Paths.get(fileName)) > 0) {
                RandomAccessFile file = new RandomAccessFile(fileName, "r");
                file.seek(file.length() - 1);
                if (file.readByte() != '\n') {
                    fileWriter.write("\n");
                }
                file.close();
                Case.idInteger = new AtomicLong(getCaseId());
            }
        } catch (IOException e) {
            handler.errorOccurred(e, null);
        }
    }
    @SuppressWarnings(value = "unchecked")
    private Long getCaseId() throws IOException {
        if (Files.exists(Paths.get(meta))) {
            HashMap<String,String> met = mapper.readValue(new File(meta),HashMap.class);
            return Long.parseLong(met.get("string_count")) + 1;
        }
        BufferedReader reader = Files.newBufferedReader(Paths.get(fileName));
        String stringCase;
        Case _case = null;
        while (reader.ready()) {
            stringCase = reader.readLine();
            try {
                mapper.readValue(stringCase, Case.class);
                _case = mapper.readValue(stringCase, Case.class);
            } catch (Exception ignored) {
            }
        }
        reader.close();
        if (_case != null) {
            return _case.getId() + 1;
        }
        return 0L;

    }

    private synchronized Case poll() {
        return cases.poll();
    }

    @Override
    public void run() {
        try {
            while (!isScrappingOver) {
                if (cases.isEmpty())  {
                    ThreadHelper.sleep(10);
                }
                else {
                    Case _case = poll();
                    update(_case);
                }
            }
            while (!cases.isEmpty()) {
                Case _case = cases.poll();
                if (_case == null) break;
                update(_case);
            }
        }
        catch (IOException e) {
            handler.errorOccurred(e, this);
        }
        finally {
            try {
                afterExecute();
                csvConverterService.convertJsonToCSV();
            } catch (IOException e) {
                handler.errorOccurred(e, this);
            }
        }
    }

    @Override
    public void createMeta() throws IOException {
        HashMap<String,String> properties = new HashMap<>();
        properties.put("string_count",String.valueOf(Case.idInteger.get()-1));
        properties.putAll(getBasicProperties());
        writeMeta(properties);
    }

    private void renew() throws IOException {
        fileWriter = new FileWriter(fileName, StandardCharsets.UTF_8, true);
    }

    private void update(Case _case) throws IOException {
        _case.setId(Case.idInteger.getAndIncrement());
        mapper.writeValue(fileWriter,_case);
        renew();
        fileWriter.write("\n");
    }

    @Override
    protected void close() {
        try {
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
