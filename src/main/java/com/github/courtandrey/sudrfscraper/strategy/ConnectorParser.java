package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.configuration.ApplicationConfiguration;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.CourtConfiguration;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.SearchPattern;
import com.github.courtandrey.sudrfscraper.dump.model.Case;
import com.github.courtandrey.sudrfscraper.service.CasesPipeLine;
import com.github.courtandrey.sudrfscraper.service.SeleniumHelper;
import com.github.courtandrey.sudrfscraper.service.logger.LoggingLevel;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public abstract class ConnectorParser implements Parser{
    protected CourtConfiguration cc;
    private final RequestBuilder builder;

    public ConnectorParser(CourtConfiguration cc) {
        this.cc = cc;
        builder = new RequestBuilder(cc);
    }

    String getJsoupText(String url) throws IOException {
        try(CloseableHttpClient httpClient =  HttpClients.custom().disableAutomaticRetries().
                setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(30*1000)
                        .setConnectionRequestTimeout(30*1000)
                        .setSocketTimeout(30*1000).build()).build()) {
            HttpGet get = builder.get(url,cc.getSearchString().replace("http://",""));
            HttpResponse response = httpClient.execute(get);
            String htmlString = EntityUtils.toString(response.getEntity());
            Document decision = Jsoup.parse(htmlString);
            return parseText(decision);
        }
    }

    String getSeleniumText(String href) {
        Document doc = Jsoup.parse(SeleniumHelper.getInstance().getPage(href,setWaitTime()));
        return parseText(doc);
    }

    private int setWaitTime() {
        if (cc.getSearchPattern() == SearchPattern.DEPRECATED_SECONDARY_PATTERN ||
                cc.getSearchPattern() == SearchPattern.SECONDARY_PATTERN) {
            return 3;
        } else {
            return 10;
        }
    }

    @Override
    public void scrapTextsAndFlush(Set<Case> resultCases, CasesPipeLine casesPipeLine) {
        Set<Case> chunk = new HashSet<>();
        String chunkSizeString = ApplicationConfiguration.getInstance().getProperty("dev.pipeline.chunk_size");
        int chunkSize = 10000;
        if (chunkSizeString != null) {
            try {
                chunkSize = Integer.parseInt(chunkSizeString);
            } catch (Exception ignored) {}
        }
        while (!resultCases.isEmpty()) {
            for (Case _case:resultCases) {
                chunk.add(_case);
                if (chunk.size() == chunkSize) {
                    break;
                }
            }
            SimpleLogger.log(LoggingLevel.INFO, String.format(Message.CHUNK.toString(),resultCases.size(),cc.getName()));
            chunk = scrapTexts(chunk);
            casesPipeLine.offer(chunk);
            resultCases.removeAll(chunk);
            chunk.clear();
        }
    }

    protected String cleanUp(String dirtyString) {
        dirtyString = dirtyString.replaceAll("\\s{2,}", "");
        dirtyString = dirtyString.replaceAll("\n+", "\n");
        dirtyString = dirtyString.replaceAll(" ","");
        return dirtyString.replaceAll("\u200B","");
    }
}
