package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.CourtConfiguration;
import com.github.courtandrey.sudrfscraper.service.SeleniumHelper;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class Connector {
    protected CourtConfiguration courtConfiguration;
    private final RequestBuilder builder;

    public Connector(CourtConfiguration courtConfiguration) {
        this.courtConfiguration = courtConfiguration;
        builder = new RequestBuilder(courtConfiguration.getLevel());
    }

    public Document getDocument(String url) throws IOException {
        switch (courtConfiguration.getConnection()) {
            case SELENIUM -> {
                return getSeleniumText(url);
            }
            case REQUEST -> {
                return getRequestText(url);
            }
            default -> throw new UnsupportedOperationException("Unknown Connection Type");
        }
    }

    Document getRequestText(String url) throws IOException {
        try(CloseableHttpClient httpClient =  HttpClients.custom().disableAutomaticRetries().
                setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(30*1000)
                        .setConnectionRequestTimeout(30*1000)
                        .setSocketTimeout(30*1000).build()).build()) {
            HttpGet get = builder.get(url,courtConfiguration.getSearchString().replace("http://",""));
            HttpResponse response = httpClient.execute(get);
            String htmlString = EntityUtils.toString(response.getEntity());
            return Jsoup.parse(htmlString);
        }
    }

    Document getSeleniumText(String href) {
        return Jsoup.parse(SeleniumHelper.getInstance().getPage(href,10));
    }
}
