package com.github.courtandrey.sudrfscraper.web;

import com.github.courtandrey.sudrfscraper.configuration.ApplicationConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApplicationController {
    @GetMapping("/")
    public String welcome(Model model) {
        if (isScrapingProceeds) return "scraping";
        if (ApplicationConfiguration.getInstance().getProperty("user.hide_info") != null &&
                ApplicationConfiguration.getInstance().getProperty("user.hide_info").equals("true")) {
            return search(model);
        }
        return "info";
    }
    @GetMapping("/search")
    public String search(Model model) {
        if (isScrapingProceeds) return "scraping";
        model.addAttribute("us_dr",ApplicationConfiguration.getInstance().getProperty("basic.result.path"));
        return "searchrequest";
    }
    @GetMapping("/scraping")
    public String scraping(Model model) {
        if (!isScrapingProceeds) return welcome(model);
        return "scraping";
    }

    private boolean isScrapingProceeds = false;

    public void setScrapingProceeds(boolean scrapingProceeds) {
        isScrapingProceeds = scrapingProceeds;
    }
}
