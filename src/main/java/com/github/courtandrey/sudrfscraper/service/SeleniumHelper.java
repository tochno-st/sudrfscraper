package com.github.courtandrey.sudrfscraper.service;

import com.github.courtandrey.sudrfscraper.configuration.ApplicationConfiguration;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.List;

public class SeleniumHelper {

    private static WebDriver wd;
    private static SeleniumHelper sh;
    private static WebDriver appHolder;

    public static void setAppHolder(WebDriver appHolder) {
        SeleniumHelper.appHolder = appHolder;
    }

    public static synchronized boolean isActive() {
        return wd != null;
    }

    private SeleniumHelper() {}

    public synchronized void refresh() {
        if (wd == null) reset();
        wd.navigate().refresh();
    }

    public synchronized WebElement findElement(By by) {
        if (wd == null) reset();
        return wd.findElement(by);
    }

    public synchronized List<WebElement> findElements(By by) {
        if (wd == null) reset();
        return wd.findElements(by);
    }

    public static synchronized WebDriver createDriver() {
        String os = System.getProperty("os.name");
        String nul = "nul";
        if (os.toLowerCase().contains("linux")) {
            nul = "/dev/null";
            System.setProperty("webdriver.gecko.driver", ApplicationConfiguration.getUsrDir() + "/src/main/resources/linux/geckodriver");
        }
        else if (os.toLowerCase().contains("windows")) {
            System.setProperty("webdriver.gecko.driver", ApplicationConfiguration.getUsrDir() + "/src/main/resources/windows/geckodriver.exe");
        } else if (os.toLowerCase().contains("mac")) {
            nul = "/dev/null";
            System.setProperty("webdriver.gecko.driver", ApplicationConfiguration.getUsrDir() + "/src/main/resources/macOS/geckodriver");
        }

        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, nul);
        FirefoxOptions options = new FirefoxOptions();
        WebDriver wd = new FirefoxDriver(options);
        wd.manage().timeouts().pageLoadTimeout(Duration.ofMinutes(1));
        wd.manage().timeouts().scriptTimeout(Duration.ofMinutes(1));
        wd.manage().timeouts().implicitlyWait(Duration.ofMinutes(1));
        return wd;
    }

    public static synchronized SeleniumHelper getInstance() {
        if (sh == null) {
            String os = System.getProperty("os.name");
            String nul = "nul";
            if (os.toLowerCase().contains("linux")) {
                nul = "/dev/null";
                System.setProperty("webdriver.gecko.driver", ApplicationConfiguration.getUsrDir() + "/src/main/resources/linux/geckodriver");
            }
            else if (os.toLowerCase().contains("windows")) {
                System.setProperty("webdriver.gecko.driver", ApplicationConfiguration.getUsrDir() + "/src/main/resources/windows/geckodriver.exe");
            } else if (os.toLowerCase().contains("mac")) {
                nul = "/dev/null";
                System.setProperty("webdriver.gecko.driver", ApplicationConfiguration.getUsrDir() + "/src/main/resources/macOS/geckodriver");
            }

            System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, nul);
            FirefoxOptions options = new FirefoxOptions();
            options.addArguments("--headless");
            options.setPageLoadStrategy(PageLoadStrategy.EAGER);
            sh = new SeleniumHelper();
            wd = new FirefoxDriver(options);
            wd.manage().timeouts().pageLoadTimeout(Duration.ofMinutes(1));
            wd.manage().timeouts().scriptTimeout(Duration.ofMinutes(1));
        }
        return sh;
    }

    private static void reset() {
        wd = new FirefoxDriver();
        wd.manage().timeouts().pageLoadTimeout(Duration.ofMinutes(1));
        wd.manage().timeouts().scriptTimeout(Duration.ofMinutes(1));
    }

    public synchronized String getCurrentUrl() {
        if (wd == null) reset();
        if (wd.getCurrentUrl() == null) throw new UnsupportedOperationException();
        return wd.getCurrentUrl();
    }

    public synchronized String getPageSource() {
        if (wd == null) reset();
        if (wd.getCurrentUrl() == null) throw new UnsupportedOperationException();
        return wd.getPageSource();
    }

    public synchronized String getPage(String sourceUrl, Integer waitTime) {
        if (wd == null) reset();

        wd.get(sourceUrl.replaceFirst("http","https"));

        if (waitTime != null) {
            ThreadHelper.sleep(waitTime);
        }

        if (wd.getPageSource() == null) throw new TimeoutException();

        return wd.getPageSource();
    }

    public static synchronized void endSession() {
        if (isActive()) wd.quit();
        sh = null;
    }

    public static synchronized void killApp() {
        appHolder.quit();
    }

}
