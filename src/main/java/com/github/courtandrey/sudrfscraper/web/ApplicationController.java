package com.github.courtandrey.sudrfscraper.web;

import com.github.courtandrey.sudrfscraper.configuration.ApplicationConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApplicationController {
    @GetMapping("/")
    public String welcome() {
        if (isScrapingProceeds) return "scraping";
        if (ApplicationConfiguration.getInstance().getProperty("user.hide_info") != null &&
                ApplicationConfiguration.getInstance().getProperty("user.hide_info").equals("true")) {
            return search();
        }
        return "info";
    }
    @GetMapping("/search")
    public String search() {
        if (isScrapingProceeds) return "scraping";
        return "searchrequest";
    }
    @GetMapping("/scraping")
    public String scraping() {
        if (!isScrapingProceeds) return welcome();
        return "scraping";
    }

    private boolean isScrapingProceeds = false;

    public void setScrapingProceeds(boolean scrapingProceeds) {
        isScrapingProceeds = scrapingProceeds;
    }
}
