## SUDRFScraper (User-friendly interactive interface to parse Russian court decisions)

Project is made by **Andrew Sudarkin** in collaboration with [**To Be Precise**](https://tochno.st/).

> Sometimes requests from foreign IPs are blocked. So using a Russian IP (inside Russia or via proxy server / VPN) is needed.

### DESCRIPTION

Among Russian independent journalists, the official aggregator bsr.sudrf.ru, known as "Pravosudie", is associated with only one feeling — pain 🤕.
It is script-based, it is slow, it is down half of the time.

So the solution was as obvious as it was necessary. Just redo the aggregation from the courts' websites instead of using a script built on our taxes.

The websites of the courts use different interfaces. Some are using scripts, so selenium is needed anyway. More important for a user: some courts have CAPTCHA, though its implementation cannot possibly be dumber (basically, you need to enter one captcha for a whole region).

Nevertheless, SUDRFScraper wins in comparison with "Pravosudie".
It is faster due to its mainly request-based nature and multi-threading.
It is more stable: there are always courts which are down, but it is bearable considering that the entire "Pravosudie" tends to be down.

Also, SUDRFScraper **provides a UI, so there's no need to do any coding**.

### SETUP DETAILS


📷 Watch the [instructional video](https://www.youtube.com/watch?v=TDDejU0ap14&feature=youtu.be) 

1. Go to https://github.com/tochno-st/sudrfscraper/releases/. Find the latest release and download the version corresponding to your Operating System.
2. Unzip the archive.
3. If you are a Windows user, you can start the application with a double-click on the run.bat script. 
Otherwise, you should open the terminal and enter /path/to/run.sh (full path to file run.sh)

### IMPORTANT THINGS TO KNOW BEFORE USING

Scraper uses Firefox WebDriver, so you should have the Firefox browser. If you see errors about WebDriver in logs, follow these instruction that may help:

- Visit [https://github.com/mozilla/geckodriver/releases](https://github.com/mozilla/geckodriver/releases) and download the driver for your OS.
- Replace the driver in ./src/main/resources/"name-of-your-OS"/.

Scraped cases are from courts of general jurisdiction.

You are highly not recommended to use the "Text to Search Within Acts" field. 
Most of the courts don't support this field, so search will be executed among ALL cases with published decision up to this very day.
Only use this field in combination with others.

You can only search through one type of articles. If it is not set, the search will run for criminal articles.

You are highly not recommended to use the bold administrative article search.
Administrative and Civil cases are aggregated simultaneously in many courts, so you need to specify the CAS article or filter your results.

### EXECUTION INFO

As for execution you have these search options:

1. *Start and end dates*. "Result" means the case is finished, though there might not be a published decision. These are the only available date params for now. I do, however, understand how important the entry date can be for some cases. Don't use these fields if you want unfinished cases (for some reason).
2. *Text-in-Decision field*. Obvious. Just don't forget the info covered in the previous chapter.
3. The main option: *article*. SUDRFScraper supports Criminal Articles, Administrative Offense Articles, Administrative Proceedings, Casefile Proceedings and Civil Proceedings. Just don't forget the info covered in the previous chapter (x2).

There are only two dump types supported: MySQL-table and line-by-line JSON document.

You are also able to configure these options:

1. *Directory path*. The path to the desirable result directory.
2. *Continue*. If you want to continue a previous session (scrap previously inactive courts or continue an aborted scraping), you can check this option. You should enter the project name and directory of the previous scraping. If there is no project set, scraping will continue from the most recent session results.
3. *Cases filter*. Use SOFT for collecting subArticles for entered Articles, e.g., 20.3 collects 20.3.3. Use STRICT for collecting only the specified article, e.g., 20.3 doesn't collect 20.3.3.
4. *Court Level* and *Court Region filter*.

By the end of execution, you are given a summary. It is a list of issues that occurred, so you may know how many cases you could miss during the scraping. You can also check the logs to find information about the issues. If there are many issues that do not include server problems (Inactive court, Connection error), like possible different interfaces, I will be glad if you contact me for further improvement of the system.

### UPDATES

ver.0.1.4 CAS-UPDATE. Feat: implementation of search based on administrative articles. Small bug fixes and refactoring.

ver 0.1.5 configuration-update. You can change results dump directory modifying application.properties. Small bug fixes and refactoring.

ver.0.1.6 configuration-update. Modified configuration with "use.court.history" configuration param. Now user won't be trashed with thousands of files. Added some tests.
Massive refactoring.

ver.0.1.7 configuration-update. Modified configuration with default sql connection parameters. Massive refactoring.

ver.0.1.8 captcha-update. Feat: added regions to configuration. Fixed captcha-related bugs. Updated strategies and court configuration for solving new issues. 

ver.0.1.9 content-update. Feat: fixed broken courts, removed courts that ceased to exist, replaced renamed courts, added garrison courts and mosgorsud. Configs: added sorting by court level.

ver.0.1.10 flexibility-update. Feat: Storing configs in results and reusing them in future continuing scraping to increase flexibility. Refactoring and bug fixes.

ver.0.2.0 UI-update. Feat: added new HTML-based user interface. Migrated to Spring Boot.

ver.0.2.1 fix-update. UI bug fixes. Article-related bug fixes.

ver.0.2.2 material-proceeding-update. Added Casefile ("Material") Proceeding.

ver.0.2.3 cas-fix-update. Fixed bugs with mosgorsud cas code. Added lawbook for CAS.

ver.0.2.4 civil-proceeding-update. Added Civil Proceeding.

ver.0.2.4-1 performance enhancement. Enhanced performance for VPN environment.

ver.0.2.5 appellation-update. Added appellation instance.

CONTACT ME:

Email: sudarkinandrew@gmail.com
