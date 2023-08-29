package com.github.courtandrey.sudrfscraper.service;

import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.CourtConfiguration;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.Level;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.Instance;
import com.github.courtandrey.sudrfscraper.exception.CaptchaException;
import com.github.courtandrey.sudrfscraper.exception.InitializationException;
import com.github.courtandrey.sudrfscraper.service.logger.LoggingLevel;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;
import com.github.courtandrey.sudrfscraper.strategy.Connection;
import com.github.courtandrey.sudrfscraper.view.View;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Properties;

import static com.github.courtandrey.sudrfscraper.service.Constant.PATH_TO_CAPTCHA;
import static com.github.courtandrey.sudrfscraper.service.Constant.UA;

public class CaptchaPropertiesConfigurator {
    private static SeleniumHelper sh;
    private final CourtConfiguration cc;

    private static View view;
    private final Properties props;
    protected static String wellItWorkedOnce;

    public static void setView(View view) {
        CaptchaPropertiesConfigurator.view = view;
    }

    public CaptchaPropertiesConfigurator(CourtConfiguration cc) {
        this.cc = cc;
        props = getProps();
    }

    public static void configureCaptcha(CourtConfiguration cc, boolean didWellItWorkedOnceUsed, Connection connection, Instance i) throws InterruptedException {
        switch (connection) {
            case SELENIUM -> configureCaptchaWithSelenium(cc, didWellItWorkedOnceUsed,i);
            case REQUEST -> configureCaptchaWithRequests(cc, didWellItWorkedOnceUsed,i);
            default -> throw new UnsupportedOperationException("Unknown type of connection");
        }
    }

    private static void configureCaptchaWithRequests(CourtConfiguration cc, boolean didWellItWorkedOnceUsed, Instance in) throws InterruptedException {
        CaptchaPropertiesConfigurator cpc = new CaptchaPropertiesConfigurator(cc);

        if (cpc.checkProperties(didWellItWorkedOnceUsed)) return;

        String urlFprCaptcha = (new URLCreator(cc)).createUrlForCaptcha(in);

        String captcha = null;
        Document document = null;
        for (int i = 0; i < 10; i++) {
            try {
                document = Jsoup.connect(urlFprCaptcha).userAgent(UA.toString()).get();
                break;
            } catch (IOException e) {
                ThreadHelper.sleep(3);
            }
        }
        if (document == null) {
            throw new TimeoutException();
        }
        try {
            for (Element e:document.getElementsByTag("tr")) {
                Elements captchaid = e.getElementsByAttributeValue("name","captchaid");
                if (captchaid.isEmpty()) continue;
                String dataUrl = e.getElementsByTag("img").attr("src");
                byte[] dataBytes = Base64.getDecoder().decode(dataUrl.replaceFirst("data:.+,","").trim());
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(dataBytes));
                captcha = view.showCaptcha(image);
                break;
            }
            if (captcha == null) {
                for (Element e:document.getElementsByClass("form-item general-item category-item")) {
                    if (!e.text().contains("Проверочный код")) continue;
                    String dataUrl = e.getElementsByTag("img").get(0).attr("src");
                    String replaced = dataUrl.replaceFirst("data:.+,","").trim();
                    byte[] dataBytes = Base64.getDecoder().decode(replaced);
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(dataBytes));
                    captcha = view.showCaptcha(image);
                    break;
                }
            }
            if (captcha == null) throw new CaptchaException();
        } catch (IOException e) {
            throw new CaptchaException();
        }


        String captchaId = document.getElementsByAttributeValue("name","captchaid").attr("value");

        String value = captcha + "&" + captchaId;

        if (cc.getLevel()!= Level.REGION) {
            wellItWorkedOnce = value;
        }
        cpc.setProperty(value);
    }

    public String getCaptcha() {
        return String.valueOf(props.get(String.valueOf(cc.getId())));
    }

    private Properties getProps() {
        Properties properties = new Properties();
        try {
            if (Files.notExists(Paths.get(String.format(PATH_TO_CAPTCHA.toString(), cc.getRegion())))) {
                Files.createFile(Paths.get(String.format(PATH_TO_CAPTCHA.toString(), cc.getRegion())));
            }
            properties.load(new FileReader(String.format(PATH_TO_CAPTCHA.toString(), cc.getRegion())));
        } catch (IOException e) {
           throw new InitializationException(e);
        }
        return properties;
    }

    public static void configureCaptchaWithSelenium(CourtConfiguration cc, boolean didWellItWorkedOnceUsed, Instance i) throws InterruptedException {
        CaptchaPropertiesConfigurator cpc = new CaptchaPropertiesConfigurator(cc);

        if (cpc.checkProperties(didWellItWorkedOnceUsed)) return;

        if (sh == null) {
            sh = SeleniumHelper.getInstance();
        }

        String urlFprCaptcha = (new URLCreator(cc)).createUrlForCaptcha(i);

        if (sh.getCurrentUrl().equals(urlFprCaptcha)) {
            sh.refresh();
            ThreadHelper.sleep(10);
        } else {
            sh.getPage(urlFprCaptcha, 10);
        }

        String captcha = null;
        Document document = Jsoup.parse(sh.getPageSource());
        try {
            for (Element e:document.getElementsByClass("form-item general-item category-item")) {
                if (!e.text().contains("Проверочный код")) continue;
                String dataUrl = e.getElementsByTag("img").get(0).attr("src");
                String replaced = dataUrl.replaceFirst("data:.+,","").trim();
                byte[] dataBytes = Base64.getDecoder().decode(replaced);
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(dataBytes));
                captcha = view.showCaptcha(image);
                break;
            }
            if (captcha == null) {
                for (WebElement e:sh.findElements(By.tagName("tr"))) {
                    try {
                        e.findElement(By.name("captchaid"));
                        String dataUrl = e.findElement(By.tagName("img")).getAttribute("src");
                        byte[] dataBytes = Base64.getDecoder().decode(dataUrl.replaceFirst("data:.+,","").trim());
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(dataBytes));
                        captcha = view.showCaptcha(image);
                        break;
                    } catch (NoSuchElementException ignored) {}
                }
            }
            if (captcha == null) throw new CaptchaException();
        } catch (IOException e) {
            throw new CaptchaException();
        }

        String captchaId = sh.findElement(By.name("captchaid")).getAttribute("value");

        String value = captcha + "&" + captchaId;

        if (cc.getLevel()!= Level.REGION) {
            wellItWorkedOnce = value;
        }

        cpc.setProperty(value);
    }

    private void setProperty(String value) {
        props.setProperty(String.valueOf(cc.getId()),value);
        refresh();
    }

    private void refresh() {
        try {
            props.store(new FileWriter(String.format(PATH_TO_CAPTCHA.toString(), cc.getRegion())),"");
        } catch (IOException e) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.IOEXCEPTION_OCCURRED);
        }
    }

    private boolean checkProperties(boolean didWellItWorkedOceUsed) {
        if (props.isEmpty()) return false;
        else {
            if (wellItWorkedOnce == null) return false;
            if (didWellItWorkedOceUsed) return false;
            setProperty(wellItWorkedOnce);
        }
        return true;
    }
}