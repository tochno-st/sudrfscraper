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
        String end = "/results/";
        if (System.getProperty("os.name").contains("windows")) {
            end = "\\results\\";
        }
        model.addAttribute("us_dr",ApplicationConfiguration.getInstance().getProperty("user.work_directory") + end);
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
