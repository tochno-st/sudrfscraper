package com.github.courtandrey.sudrfscraper.service;

import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.CourtConfiguration;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.SearchPattern;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.article.AdminArticle;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.article.CASArticle;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.article.CriminalArticle;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.SearchRequest;
import com.github.courtandrey.sudrfscraper.service.logger.Message;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.time.LocalDate;

public class URLCreator {

    private String[] urls;
    private String[] endings;
    private final SearchRequest sc = SearchRequest.getInstance();
    private final CourtConfiguration cc;
    private SearchPattern pattern;

    public URLCreator(CourtConfiguration cc) {
        this.cc = cc;
        setPattern();
    }

    private void setPattern() {
        pattern=cc.getSearchPattern();
    }

    public String createUrlForCaptcha() {
        String ending = "";
        switch (pattern) {
            case VNKOD_PATTERN -> {
                switch (sc.getField()) {
                    case CRIMINAL -> ending = "/modules.php?name=sud_delo&name_op=sf&srv_num=1&_deloId=1540006&_caseType=0&_new=0";
                    case ADMIN, CAS -> ending = "/modules.php?name=sud_delo&srv_num=1&name_op=sf&delo_id=1540005&_caseType=0&_new=0";
                }
            }
            case PRIMARY_PATTERN -> {
                switch (sc.getField()) {
                    case CRIMINAL -> ending = "/modules.php?name=sud_delo&srv_num=1&name_op=sf&delo_id=1540006";
                    case ADMIN, CAS -> ending = "/modules.php?name=sud_delo&srv_num=1&name_op=sf&delo_id=1540005";
                }
            }
        }
        return cc.getSearchString() + ending;
    }

    public String[] createUrls() {
        getEndings();
        makeSearchConfiguration();
        replaceVnkod();

        if (cc.isHasCaptcha())
            replaceCaptcha();

        getTogether();
        return urls;
    }

    private void replaceCaptcha() {
        String captcha = (new CaptchaPropertiesConfigurator(cc)).getCaptcha();
        if (captcha.split("&").length < 2) return;
        for (int i = 0; i < endings.length; i++) {
            endings[i] = endings[i] + "&captcha="+captcha.split("&")[0];
            endings[i] = endings[i]+"&captchaid="+captcha.split("&")[1];
        }
    }

    public String returnEnding(int indexUrl) {
        return pattern.getPattern(sc.getField())[indexUrl];
    }

    private void getEndings() {
        if (cc.getWorkingUrl().get(sc.getField()) == null) {
            endings = pattern.getPattern(sc.getField());
        } else {
            endings = new String[]{cc.getWorkingUrl().get(sc.getField())};
        }
        urls = new String[endings.length];
    }


    private void replaceVnkod() {
        switch (pattern) {
            case SECONDARY_PATTERN, DEPRECATED_SECONDARY_PATTERN -> replaceVnkodForSecondaryPattern();
            case PRIMARY_PATTERN, VNKOD_PATTERN -> replaceVnkodForPrimaryPatterns();
            default -> {}
        }
    }

    private void replaceVnkodForPrimaryPatterns() {
        for (int i = 0; i < endings.length; i++) {
            endings[i] = endings[i].replace("vnkod=", "vnkod=" + cc.getVnkod());
        }
    }

    private void replaceVnkodForSecondaryPattern() {
        for (int i = 0; i < endings.length; i++) {
            endings[i] = endings[i].replace("$VNKOD", cc.getVnkod());
        }
    }

    private void getTogether() {
        for (int i = 0; i < endings.length; i++) {
            urls[i] = cc.getSearchString()+endings[i];
        }
    }

    private void makeSearchConfiguration() {
        switch (pattern) {
            case SECONDARY_PATTERN,DEPRECATED_SECONDARY_PATTERN -> makeSearchConfigurationForSecondaryPattern();
            case VNKOD_PATTERN,PRIMARY_PATTERN,DEPRECATED_PRIMARY_PATTERN -> makeSearchConfigurationForPrimaryPatterns();
            case MOSGORSUD_PATTERN -> makeSearchConfigurationForMosGorSud();
        }
    }

    private void makeSearchConfigurationForMosGorSud() {
        if (sc.getArticle() != null) {
            String articlePart = getArticlePartForMosgorsudPattern();
            if (sc.getArticle() instanceof CASArticle) {
                endings[0] = endings[0].replace("category=","category="+articlePart);
            }
            else {
                endings[0] = endings[0].replace("codex=","codex="+articlePart);
            }
        }

        if (sc.getText() != null) {
            endings[0] = endings[0].replace("documentText=","documentText="+sc.getText());
        }

        if (sc.getResultDateFrom() != null) {
            endings[0] = endings[0].replace("caseFinalDateFrom=","caseFinalDateFrom="+sc.getResultDateFrom());
        }

        if (sc.getResultDateTill()!=null) {
            endings[0] = endings[0].replace("caseFinalDateTo=","caseFinalDateTo="+sc.getResultDateTill());
        }
    }

    private String getArticlePartForMosgorsudPattern() {
        if (sc.getArticle() instanceof CriminalArticle) {
            return getCriminalArticlePartForMosGorSudPattern();
        }
        else if (sc.getArticle() instanceof AdminArticle) {
            return getAdminArticlePartForMosGorSudPattern();
        }
        else if (sc.getArticle() instanceof CASArticle) {
            return getCASArticleForMosGorSudPatterns();
        }
        throw new UnsupportedOperationException(Message.UNKNOWN_ARTICLE.toString());
    }

    private String getCASArticleForMosGorSudPatterns() {
        return ((CASArticle) sc.getArticle()).getMosgorsudCode();
    }
    private String getAdminArticlePartForMosGorSudPattern() {
        AdminArticle article = (AdminArticle) sc.getArticle();
        StringBuilder stringBuilder = new StringBuilder("ст. "+article.getChapter()+"."+article.getArticle());
        if (article.getSubArticle()!=0) {
            stringBuilder.append(".").append(article.getSubArticle());
        }
        if (article.getPart()!=0) {
            stringBuilder.append("+ч.").append(article.getPart());
        }
        return stringBuilder.toString();
    }

    private String getCriminalArticlePartForPrimaryPatterns() {
        CriminalArticle article = (CriminalArticle) sc.getArticle();
        StringBuilder articlePart = new StringBuilder(String.valueOf(article.getArticle()));
        if (article.getSubArticle() != 0) {
            articlePart.append(".").append(article.getSubArticle());
        }
        if (article.getPart() != 0) {
            articlePart.append("+%F7.").append(article.getPart());
        }
        if (article.getLetter() != 0) {
            Charset neededCharset = Charset.forName("windows-1251");
            String letter = String.valueOf(article.getLetter());
            ByteBuffer buffer = neededCharset.encode(CharBuffer.wrap(letter));
            int ch = buffer.get(0) & 0xff;
            String hex = Integer.toHexString(ch);
            articlePart.append("+%EF.").append(hex.toUpperCase());
        }
        return articlePart.toString();
    }
    private String getCriminalArticlePartForMosGorSudPattern() {
        CriminalArticle article = (CriminalArticle) sc.getArticle();
        StringBuilder articlePart = new StringBuilder(String.valueOf(article.getArticle()));
        if (article.getSubArticle() != 0) {
            articlePart.append(".").append(article.getSubArticle());
        }
        if (article.getPart() != 0) {
            articlePart.append("+ч.").append(article.getPart());
        }
        if (article.getLetter() != 0) {
            articlePart.append("%2C+п.+").append(article.getLetter());
        }
        return articlePart.toString();
    }

    private String getAdminArticlePartForPrimaryPatterns() {
        AdminArticle article = (AdminArticle) sc.getArticle();
        StringBuilder articlePart = new StringBuilder(article.getChapter() +"."+article.getArticle());
        if (article.getSubArticle() != 0) {
            articlePart.append(".").append(article.getSubArticle());
        }
        if (article.getPart() != 0) {
            articlePart.append("+%F7.").append(article.getPart());
        }
        return articlePart.toString();
    }

    private String getArticlePartForPrimaryPatterns() {
        if (sc.getArticle() instanceof CriminalArticle) {
            return getCriminalArticlePartForPrimaryPatterns();
        }
        else if (sc.getArticle() instanceof AdminArticle) {
            return getAdminArticlePartForPrimaryPatterns();
        }
        else if (sc.getArticle() instanceof CASArticle) {
            return getCASArticleForPrimaryPatterns();
        }
        throw new UnsupportedOperationException(Message.UNKNOWN_ARTICLE.toString());
    }

    private String getCASArticleForPrimaryPatterns() {
        StringBuilder sb = new StringBuilder();
        String mainPart = sc.getArticle().getMainPart();
        Charset neededCharset = Charset.forName("windows-1251");
        for (int i = 0; i < mainPart.length(); i++) {
            if (Character.isWhitespace(mainPart.charAt(i))) {
                sb.append("+");
            }
            else {
                String letter = String.valueOf(mainPart.charAt(i));
                ByteBuffer buffer = neededCharset.encode(CharBuffer.wrap(letter));
                int ch = buffer.get(0) & 0xff;
                String hex = Integer.toHexString(ch);
                sb.append("%").append(hex.toUpperCase());
            }
        }
        return sb.toString();
    }

    private void makeSearchConfigurationForPrimaryPatterns() {
        if (sc.getArticle() != null) {
            String articlePart = getArticlePartForPrimaryPatterns();
            for (int i = 0; i < endings.length; i++) {
                try {
                    endings[i] = endings[i].replace("LAW_ARTICLESS=",
                            "LAW_ARTICLESS=" + articlePart);
                } catch (Exception ignored) {}
                try {
                    endings[i] = endings[i].replace("LAW_ARTICLESS=".toLowerCase(),
                            "law_articless=" + articlePart);
                } catch (Exception ignored) {}
                try {
                    endings[i] = endings[i].replace("lawbookarticles%5B%5D=",
                            "lawbookarticles%5B%5D=" + articlePart);
                } catch (Exception ignored) {}

                if (sc.getArticle() instanceof CASArticle && !checkFields()) {
                    sc.setEntryDateTill(LocalDate.now());
                    try {
                        endings[i] = endings[i].replace("ENTRY_DATE2D=",
                                "ENTRY_DATE2D=" + sc.getEntryDateTill());
                    } catch (Exception ignored) {}
                    try {
                        endings[i] = endings[i].replace("entry_date2d=",
                                "entry_date2d=" + sc.getEntryDateTill());
                    } catch (Exception ignored) {}
                }

            }
        }

        if (sc.getText() != null) {
            if (sc.getPublishedDateTill() == null) {
                if (sc.getResultDateTill() == null) {
                    sc.setPublishedDateTill(LocalDate.now());
                }
                else  {
                    sc.setStringPublishedDateTill(sc.getResultDateTill());
                }
            }

            for (int i = 0; i < endings.length; i++) {
                try {
                    endings[i] = endings[i].replace("PUBL_DATE2D=",
                            "PUBL_DATE2D=" + sc.getPublishedDateTill());
                } catch (Exception ignored) {}
                try {
                    endings[i] = endings[i].replace("publ_date2d=",
                            "publ_date2d=" + sc.getPublishedDateTill());
                } catch (Exception ignored) {}
            }
        }

        if (sc.getResultDateFrom() != null) {
            for (int i = 0; i < endings.length; i++) {
                try {
                    endings[i] = endings[i].replace("case__RESULT_DATE1D=",
                            "case__RESULT_DATE1D=" + sc.getResultDateFrom());
                } catch (Exception ignored) {}
                try {
                    endings[i] = endings[i].replace("case__result_date1d=",
                            "case__result_date1d=" + sc.getResultDateFrom());
                } catch (Exception ignored) {}
            }
        }

        if (sc.getResultDateTill()!=null) {
            for (int i = 0; i < endings.length; i++) {
                try {
                    endings[i] = endings[i].replace("case__RESULT_DATE2D=",
                            "case__RESULT_DATE2D=" + sc.getResultDateTill());
                } catch (Exception ignored) {}
                try {
                    endings[i] = endings[i].replace("case__result_date2d=",
                            "case__result_date2d=" + sc.getResultDateTill());
                } catch (Exception ignored) {}
            }
        }
    }

    private String getDateForSecondaryPattern() {
        if (sc.getResultDateFrom() == null && sc.getResultDateTill() == null) return null;
        String datePart = "{\\\"name\\\":\\\"case_user_doc_result_date\\\",\\\"operator\\\":\\\"B\\\",";
        if (sc.getResultDateFrom() != null) {
            String date1 = sc.getResultDateFrom();
            String[] splits = date1.split("\\.");
            String sb = splits[2] + "-" + splits[1] + "-" + splits[0];
            datePart += String.format("\\\"query\\\":\\\"%sT00:00:00\\\",",sb);
        }
        if (sc.getResultDateTill() != null) {
            String date2=sc.getResultDateTill();
            String[] splits = date2.split("\\.");
            String sb = splits[2] + "-" + splits[1] + "-" + splits[0];
            datePart += String.format("\\\"sQuery\\\":\\\"%sT00:00:00\\\",",sb);
        }
        datePart += "\\\"fieldName\\\":\\\"case_user_doc_result_date\\\"},";
        return datePart;
    }

    private String getCriminalArticleForSecondaryPattern() {
        CriminalArticle article = (CriminalArticle) sc.getArticle();
        String articlePart = "";
        String subArticle = article.getSubArticle() != 0 ? "." + article.getSubArticle() : "";
        String part = article.getPart() != 0 ? " Часть " + article.getPart() : "";
        String letter = article.getLetter() != 0 ? " п." + article.getLetter() : "";
        articlePart += String.format("{\\\"name\\\":\\\"u_case_user_article\\\",\\\"operator\\\":\\\"EX\\\",\\\"query\\\":\\\"Статья %d%s%s%s\\\",\\\"sQuery\\\":null}",
                article.getArticle(), subArticle, part, letter);
        articlePart += "],\\\"mode\\\":\\\"AND\\\",\\\"name\\\":\\\"Уголовные дела\\\",\\\"typesMode\\\":\\\"AND\\\"},";
        if (checkFields()) {
            articlePart += "{\\\"fieldRequests\\\":[";
        }
        return articlePart;
    }

    private String getAdminArticleForSecondaryPattern() {
        AdminArticle article = (AdminArticle) sc.getArticle();
        String articlePart = "";
        int chapter = article.getChapter();
        String art = "." + article.getArticle();
        String subArticle = article.getSubArticle() != 0 ? "."+article.getSubArticle() : "";
        String part = article.getPart() != 0 ? " ч."+article.getPart() : "";
        articlePart += String.format("{\\\"name\\\":\\\"case_common_parts_law_article_cat\\\",\\\"operator\\\":\\\"SEW\\\",\\\"query\\\":\\\"%d%s%s%s \\\",\\\"fieldName\\\":\\\"case_common_parts_law_article_cat\\\"},",
                chapter, art, subArticle, part);
        return articlePart;
    }

    private String getArticlePartForSecondaryPattern() {
        if (sc.getArticle() instanceof CriminalArticle) {
            return getCriminalArticleForSecondaryPattern();
        }
        else if (sc.getArticle() instanceof AdminArticle) {
            return getAdminArticleForSecondaryPattern();
        }
        else if (sc.getArticle() instanceof CASArticle) {
            return getCASArticleForSecondaryPattern();
        }
        throw new UnsupportedOperationException(Message.UNKNOWN_ARTICLE.toString());
    }

    private String getCASArticleForSecondaryPattern() {
        CASArticle cas = (CASArticle) sc.getArticle();
        String article = cas.getPartOfCas();
        String articlePart = String.format("{\\\"name\\\":\\\"g_case_user_category\\\",\\\"operator\\\":\\\"AW\\\",\\\"query\\\":\\\"%s\\\",\\\"fieldName\\\":\\\"g_case_user_category_cat\\\"}", article);
        articlePart += "],\\\"mode\\\":\\\"AND\\\",\\\"name\\\":\\\"Гражданские и административные дела\\\",\\\"typesMode\\\":\\\"AND\\\"},";
        if (checkFields()) {
            articlePart += "{\\\"fieldRequests\\\":[";
        }
        return articlePart;
    }

    private boolean checkFields() {
        return sc.getResultDateFrom() != null || sc.getResultDateTill() != null || sc.getText() != null;
    }

    private void makeSearchConfigurationForSecondaryPattern() {
        String request = "{\\\"fieldRequests\\\":[";
        if (sc.getArticle() != null) {
            request += getArticlePartForSecondaryPattern();
        }

        String datePart = getDateForSecondaryPattern();

        if (datePart != null) {
            request+=datePart;
        }

        if (sc.getText() != null) {
            request += String.format("{\\\"name\\\":\\\"case_user_document_text_tag\\\",\\\"operator\\\":\\\"C\\\",\\\"query\\\":\\\"%s\\\",\\\"fieldName\\\":\\\"case_document_text\\\"},",sc.getText());
        }

        if (!isEndingSet()) {
            request = request.substring(0, request.length() - 1)
                    + "],\\\"mode\\\":\\\"AND\\\",\\\"name\\\":\\\"common\\\",\\\"typesMode\\\":\\\"AND\\\"}";
        }

        else {
            request =  request.substring(0, request.length() - 1);
        }

        for (int i = 0; i < endings.length; i++) {
            endings[i] = endings[i].replace("$REQUEST", request);
        }
    }

    private boolean isEndingSet() {
        return sc.getArticle() != null && !(sc.getArticle() instanceof AdminArticle) && !checkFields();
    }
}
