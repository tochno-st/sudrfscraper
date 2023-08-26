package com.github.courtandrey.sudrfscraper.service;

import com.github.courtandrey.sudrfscraper.service.logger.LoggingLevel;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.github.courtandrey.sudrfscraper.service.logger.Message.DOWNLOAD_FAILED;

public class Downloader {
    public File download(String url) {
        try {
            String[] splits = url.split("/");
            String path = Constant.PATH_TO_TEMP +splits[splits.length-1];
            return downloadUsingNIO(url, path);
        } catch (IOException e) {
            SimpleLogger.log(LoggingLevel.WARNING,DOWNLOAD_FAILED+" "+url);
        } catch (ArrayIndexOutOfBoundsException e) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.DOWNLOAD_FAILED_UNKNOWN_EXTENSION +" " + url + " ");
        }
        return null;
    }
    private File downloadUsingNIO(String urlStr, String file) throws IOException {
        try(CloseableHttpClient httpClient =  HttpClients.custom()
                .disableAutomaticRetries()
                .setDefaultRequestConfig(
                        RequestConfig
                                .custom()
                                .setConnectTimeout(30*1000)
                                .setConnectionRequestTimeout(30*1000)
                                .setSocketTimeout(30*1000)
                                .build())
                .build()) {
            HttpGet get = new HttpGet(urlStr);
            get.setHeader("User-Agent", Constant.UA.toString());
            HttpResponse response = httpClient.execute(get);
            String[] splits = Arrays.toString(response.getHeaders("Content-Disposition")).split("filename=\"")[1].split("\"")[0]
                    .split("\\.");
            String extension = splits[splits.length - 1];
            String fileName = file + "." + extension;
            Path pathOfFile = Paths.get(fileName);
            if (Files.exists(pathOfFile)) return new File(fileName);
            Path ptt = Paths.get(Constant.PATH_TO_TEMP.toString());
            if (Files.notExists(ptt)) Files.createDirectory(ptt);
            Files.createFile(pathOfFile);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
                    fileOutputStream.write(entity.getContent().readAllBytes());
                } catch (IOException e) {
                    SimpleLogger.log(LoggingLevel.WARNING, DOWNLOAD_FAILED + " " + e.getMessage());
                }
            }
            return new File(fileName);
        }


    }
}
