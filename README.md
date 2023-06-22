SUDRFScraper

DESCRIPTION:

Among russian independent journalists official aggregator bsr.sudrf.ru known as "Pravosudie" is associated with only one feeling - pain.
It is script-based, it is slow, it is off half of the time. And with all these issues it even often does its primary job badly.

So solution was as obvious as necessary. Just re-do aggregation from the courts websites instead of using script built on our taxes.

Though there still were no open-code project to do this thing. 
And it is not much of a surprise: there are no crowds aggregating court cases and the task is not as easy as it seemed to me at the start.

Websites of courts are using different interfaces. Some are using scripts, so selenium is needed anyway.
More important for a user: some courts are with CAPTCHA, though implementation as dumb as possible (basically you need to type one captcha for a whole region).

Nevertheless, SUDRFScraper keeps on winning in compression with "Pravosudie".
It is faster due to its mainly request nature and multi-threading.
It is more stable: there are always courts which are down, but it is bearable considering that whole "Pravosudie" tends to be down.
And, what to be honest, surprises me, it is more complete.

But this project is my first non-educational project so there is quite a bunch of problems.
It also should be definitely broadened. So the work only begins here.

IMPORTANT THINGS TO KNOW BEFORE USING:

Scraper uses FireFox WebDriver, so you should have firefox browser.
If you see errors about WebDriver in log file, follow the instruction that may help:
1. Visit "https://github.com/mozilla/geckodriver/releases" and download driver for your OS.
2. Replace driver in ./src/main/resources/"name-of-your-OS"/.

Scraped cases are only first-instance considered (no appellations).
Scraped cases are among courts of general jurisdiction.

You are highly not recommended to use for a search only text-in-decision field. 
Most of the courts don't support this field so search will be executed among ALL cases with published decision till this very day.
Use this field only in a bundle with others.

You can search only through one type of articles. If it is not set, search executes among criminal articles.

You are highly not recommended to use bold administrative article search.
Administrative and Civil cases are aggregated simultaneously in many courts, so you need to specify CAS article or filter your results.

EXECUTION INFO:

As for execution you have these search options: 
1. Result date starting from and till. "Result" means case is finished though there might not be a published decision. They are only available date params for now. Though I understand how important can be entry date for some cases. Don't use these fields if you want unfinished cases (for some reason).
2. Text-in-Decision field. Obvious. Just don't forget info introduced in previous chapter.
3. The main option: article. SUDRFScraper supports Criminal Articles, Administrative Offense Articles and Administrative articles. Just don't forget info introduced in previous chapter (x2). Important to know: for administrative articles you should enter mosgorsud code.

There are only two dump types supported: MySQL-table and line-by-line JSON document.

You are also able to configure these options:
1. Directory path. Path of desirable result directory.
2. Continue. If you want to continue finished scraping (scrap previously inactive courts or continue aborted scraping) you can check this option. You should enter project name and directory of previous scraping. If there are no project, scraping will continue from the most recent session results.
3. Cases filter. SOFT for collecting subArticles for entered Articles, e.g. 20.3 collects 20.3.3. STRICT for collecting only specified article, e.g. 20.3 doesn't collect 20.3.3
4. Court Level and Court region filter.

By the end of execution you are given summery info. It is a list of occurred issues, so you may know how many cases you could miss during the scraping. You also can check logs to find issues info.
If there are many issues that are not include server problems (Inactive court, Connection error) like possible different interfaces I will be glad if you contact me for further improvement of the system.

UPDATES:

ver.0.1.4 CAS-UPDATE. Feat: implementation of search based on administrative articles. Small bug fixes and refactoring.

ver 0.1.5 configuration-update. You can change results dump directory modifying application.properties. Small bug fixes and refactoring.

ver.0.1.6 configuration-update. Modified configuration with "use.court.history" configuration param. Now user won't be trashed with thousands of files. Added some tests.
Massive refactoring.

ver.0.1.7 configuration-update. Modified configuration with default sql connection parameters. Massive refactoring.

ver.0.1.8 captcha-update. Feat: added regions to configuration. Fixed captcha related bugs. Updated strategies and court configuration for solving new issues 

ver.0.1.9 content-update. Feat: fixed broken courts, removed courts that ceased to exist, replaced renamed courts, added garrison courts and mosgorsud. Configs: added sorting by court level.

ver.0.1.10 flexibility-update. Feat: Storing configs in results and reusing them in future continuing scraping to increase flexibility. Refactoring and bug fixes

ver.0.2.0 UI-update. Feat: added new HTML-based user interface. Migrated to Spring Boot.

CONTACT ME:

Email: sudarkinandrew@gmail.com
