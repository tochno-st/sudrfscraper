package com.github.courtandrey.sudrfscraper;

import com.github.courtandrey.sudrfscraper.configuration.ApplicationConfiguration;
import com.github.courtandrey.sudrfscraper.service.SeleniumHelper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.time.Duration;


@SpringBootApplication
public class SUDRFScraperApplication {
	public static void main(String[] args) {
		if (args.length == 1) ApplicationConfiguration.setUsrDir(args[0]);
		ApplicationConfiguration.getInstance();
		ApplicationConfiguration.getInstance().setProperty("basic.result.path", ApplicationConfiguration.getUsrDir() + "/results/");
		SpringApplication application = new SpringApplication(SUDRFScraperApplication.class);
		application.setLazyInitialization(true);
		application.run(args);
		openStartPage();
	}

	public static void openStartPage() {
		WebDriver wd = SeleniumHelper.createDriver();
		wd.get("localhost:8080/");
		SeleniumHelper.setAppHolder(wd);
	}

}
