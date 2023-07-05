package com.github.courtandrey.sudrfscraper.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.github.courtandrey.sudrfscraper.service.Constant.PATH_TO_CAS_MOSGORSUD_LAWBOOK;

public final class LawBookHelper {
    private static final Properties propsCas = getCasProps();

    private static final Map<String,String> propsVnkodMaterialProceeding = getVnkodMaterialProceedingProps();

    private static Map<String, String> getVnkodMaterialProceedingProps() {
        Map<String, String> stringStringMap = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(Constant.PATH_TO_UPK_LAWBOOK.toString()))) {
            while (reader.ready()) {
                String[] lineSplits = reader.readLine().split("=");
                stringStringMap.put(lineSplits[1],lineSplits[0]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stringStringMap;
    }

    private static Properties getCasProps() {
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(String.format(PATH_TO_CAS_MOSGORSUD_LAWBOOK.toString())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
    public static String getMosGorSudCodeCas(String number) {
        return propsCas.get(number) != null ? propsCas.get(number).toString() : null;
    }
    private LawBookHelper(){}

    public static String getVNKODCodeMaterialProceeding(String articlePart) {
        return propsVnkodMaterialProceeding.get(articlePart);
    }
}
