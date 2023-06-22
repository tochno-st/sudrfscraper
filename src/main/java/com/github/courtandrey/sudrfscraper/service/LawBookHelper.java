package com.github.courtandrey.sudrfscraper.service;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static com.github.courtandrey.sudrfscraper.service.Constant.PATH_TO_CAS_MOSGORSUD_LAWBOOK;

public final class LawBookHelper {
    private static final Properties propsCas = getProps();

    private static Properties getProps() {
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(String.format(PATH_TO_CAS_MOSGORSUD_LAWBOOK.toString())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
    public static String getMosGorSudCodeCas(String number) {
        return propsCas.get(number) != null ? propsCas.get(number).toString() : null;
    }

    public static boolean isMosGorSudCodeCas(String code) {
        return propsCas.containsValue(code);
    }
    private LawBookHelper(){}
}
