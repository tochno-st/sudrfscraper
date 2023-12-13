package com.github.courtandrey.sudrfscraper.strategy;

import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.Level;
import com.github.courtandrey.sudrfscraper.service.Constant;
import org.apache.http.client.methods.HttpGet;

public class RequestBuilder {
    private enum RequestFor{
        GENERAL,
        MOSGORSUD
    }
    private final RequestFor aFor;

    public RequestBuilder(Level ll) {
        if (ll == Level.MOSGORSUD) aFor = RequestFor.MOSGORSUD;
        else aFor = RequestFor.GENERAL;
    }

    public HttpGet get(String url, String host) {
        switch (aFor) {
            case GENERAL -> {
                return getGeneral(url,host);
            }
            case MOSGORSUD -> {
                return getMosgorsud(url);
            }
        }
        throw new UnsupportedOperationException("Unknown enum RequestFor type");
    }

    private HttpGet getGeneral(String url, String host) {
        HttpGet get = new HttpGet(url);
        get.setHeader("User-Agent", Constant.UA.toString());
        get.setHeader("Upgrade-Insecure-Requests","1");
        get.setHeader("Connection","keep-alive");
        get.setHeader("Host",host);
        get.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
        get.setHeader("Accept_Language","en-US,en;q=0.5");
        get.setHeader("Accept-Encoding","gzip, deflate, br");
        return get;
    }

    private HttpGet getMosgorsud(String url) {
        HttpGet get = new HttpGet(url);
        get.setHeader("User-Agent", Constant.UA.toString());
        get.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
        get.setHeader("Accept-Language","en-US,en;q=0.5");
        get.setHeader("Accept-Language","en-US,en;q=0.5");
        get.setHeader("Accept-Encoding","gzip, deflate, br");
        get.setHeader("Connection","keep-alive");
        get.setHeader("Referer","https://www.mos-gorsud.ru/");
        return get;
    }
}
