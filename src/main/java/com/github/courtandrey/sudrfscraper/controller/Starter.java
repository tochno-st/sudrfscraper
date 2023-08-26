package com.github.courtandrey.sudrfscraper.controller;

import com.github.courtandrey.sudrfscraper.configuration.ApplicationConfiguration;
import com.github.courtandrey.sudrfscraper.configuration.ConfigurationHolder;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.CourtConfiguration;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.Issue;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.Level;
import com.github.courtandrey.sudrfscraper.configuration.courtconfiguration.StrategyName;
import com.github.courtandrey.sudrfscraper.configuration.dumpconfiguration.ServerConnectionInfo;
import com.github.courtandrey.sudrfscraper.configuration.searchrequest.SearchRequest;
import com.github.courtandrey.sudrfscraper.dump.DBUpdaterService;
import com.github.courtandrey.sudrfscraper.dump.JSONUpdaterService;
import com.github.courtandrey.sudrfscraper.dump.Updater;
import com.github.courtandrey.sudrfscraper.dump.model.Case;
import com.github.courtandrey.sudrfscraper.dump.model.Dump;
import com.github.courtandrey.sudrfscraper.exception.LevelParsingException;
import com.github.courtandrey.sudrfscraper.exception.SearchRequestUnsetException;
import com.github.courtandrey.sudrfscraper.service.*;
import com.github.courtandrey.sudrfscraper.service.logger.LoggingLevel;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;
import com.github.courtandrey.sudrfscraper.strategy.*;
import com.github.courtandrey.sudrfscraper.view.View;
import com.github.courtandrey.sudrfscraper.view.ViewFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
@SuppressWarnings({"unused"})
public class Starter {
    private Thread mainThread;
    private SearchRequest searchConfiguration;
    private static ConfigurationHolder configHolder = null;
    private final LocalDateTime startDate = LocalDateTime.now();
    private static Updater updaterService;
    private CountDownLatch countDownLatch;
    private int courts;
    private int cases = 0;
    @Autowired
    private View view;
    private final SUDRFErrorHandler handler;
    private int[] selectedRegions = null;
    private Level[] levels = null;

    private StrategyName[] strategyNames = null;
    private boolean isStarted = false;

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    class SUDRFErrorHandler implements ErrorHandler {
        public synchronized void errorOccurred(Throwable e, Thread t) {
            if (e instanceof IOException) {
                view.showFrameWithInfo(ViewFrame.ERROR, Message.IOEXCEPTION_OCCURRED, e.getMessage());
            }
            else if (e instanceof ClassNotFoundException) {
                view.showFrameWithInfo(ViewFrame.ERROR, Message.DRIVER_NOT_FOUND, e.getMessage());
            }
            else if (e instanceof SQLException) {
                view.showFrameWithInfo(ViewFrame.ERROR, Message.SQL_EXCEPTION_OCCURRED, e.getMessage());
            }
            else if (e instanceof SearchRequestUnsetException && e.getMessage().equals(Message.UNKNOWN_DUMP.toString())) {
                view.showFrameWithInfo(ViewFrame.ERROR, Message.UNKNOWN_DUMP, e.getMessage());
            }
            else {
                view.showFrameWithInfo(ViewFrame.ERROR, Message.EXCEPTION_OCCURRED, e.getMessage());
            }

            if (t == null || !t.getName().contains("pool")) {
                view.finish();
                if (mainThread != null) {
                    mainThread.interrupt();
                }
            }
        }
    }

    public Starter() {
        handler = new SUDRFErrorHandler();
    }
    private int[] extractSelectedRegions() {
        String regionString = ApplicationConfiguration.getInstance().getProperty("basic.regions");
        if (regionString.isEmpty()) return  null;
        String[] regionsString = regionString.split(",");
        int[] regions = new int[regionsString.length];
        try {
            for (int i = 0; i < regionsString.length; i++) {
                regions[i] = Integer.parseInt(regionsString[i]);
            }
        } catch (Exception e) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.WRONG_REGIONS_FORMAT);
            return null;
        }
        return regions;
    }

    public void prepareScrapper(String dumpName, Dump dump) {
        CaptchaPropertiesConfigurator.setView(view);
        isStarted = true;
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (!t.getName().contains("pool")) handler.errorOccurred(e, null);
        });

        SimpleLogger.initLogger(dumpName, view);

        setSearchConfiguration(SearchRequest.getInstance());
        ConfigurationLoader.setDumpName(dumpName);

        selectedRegions = extractSelectedRegions();
        levels = extractSelectedLevels();
        strategyNames = extractStrategies();

        try {
            configHolder = ConfigurationHolder.getInstance();
            prepareModel();

            if (dump == Dump.SQL) {
                updaterService = new DBUpdaterService(dumpName, new SUDRFErrorHandler());
            }
            else if (dump == Dump.JSON) {
                updaterService = new JSONUpdaterService(dumpName, new SUDRFErrorHandler());
            }
            else {
                handler.errorOccurred(new SearchRequestUnsetException(Message.UNKNOWN_DUMP.toString()), null);
            }
        } catch (IOException | SQLException | ClassNotFoundException e) {
            handler.errorOccurred(e, null);
        }
    }

    private StrategyName[] extractStrategies() {
        String regionString = ApplicationConfiguration.getInstance().getProperty("dev.strategies");
        if (regionString == null || regionString.isEmpty()) return null;
        String[] regionsString = regionString.split(",");
        StrategyName[] regions = new StrategyName[regionsString.length];
        try {
            for (int i = 0; i < regionsString.length; i++) {
                regions[i] = StrategyName.parseStrategy(regionsString[i]);
            }
        } catch (Exception e) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.WRONG_STRATEGY_FORMAT);
            return null;
        }
        return regions;
    }

    private Level[] extractSelectedLevels() {
        String regionString = ApplicationConfiguration.getInstance().getProperty("basic.levels");
        if (regionString.isEmpty()) return  null;
        String[] regionsString = regionString.split(",");
        Level[] regions = new Level[regionsString.length];
        try {
            for (int i = 0; i < regionsString.length; i++) {
                regions[i] = Level.parseLevel(regionsString[i]);
            }
        } catch (LevelParsingException e) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.WRONG_LEVEL_FORMAT);
            return null;
        }
        return regions;
    }

    public void setServerConnectionInfoAndTest(String DB_URL, String user, String password) throws SQLException {
        ServerConnectionInfo info = ServerConnectionInfo.getInstance();
        info.setDbUrl(DB_URL);
        info.setUser(user);
        info.setPassword(password);
        info.testConnection();
    }

    private void setSearchConfiguration(SearchRequest searchConfiguration) {
        this.searchConfiguration = searchConfiguration;
    }
    static class StrategyThreadPoolExecutor extends ThreadPoolExecutor {
        private final Starter starter;

        public StrategyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                          TimeUnit unit, BlockingQueue<Runnable> workQueue, Starter starter) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
            this.starter = starter;
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            SUDRFStrategy strategy = (SUDRFStrategy) r;
            if (strategy.getCc().getIssue() != null) {
                SimpleLogger.log(LoggingLevel.INFO, String.format(Message.EXECUTION_STATUS_END.toString(),
                        strategy.getCc().getName(), strategy.getCc().getIssue()));
            }

            starter.update(strategy.getResultCases());

            if (t == null && r instanceof Future<?> future) {
                try {
                    if (future.isDone())
                        future.get();
                } catch (InterruptedException e) {
                    throw new ConcurrentModificationException(e);
                } catch (ExecutionException e) {
                    (new StrategyUEH()).handle(r, e.getCause());
                }
            } else if (t != null) {
                (new StrategyUEH()).handle(r, t);
            }
        }
    }

    private void prepareModel() throws IOException {
        if (!ConfigurationHelper.checkVnkods(configHolder.getCCs())) {
            configHolder.changeCCs();
        }
    }

    public SearchRequest manageSearchRequest() {
        return searchConfiguration;
    }

    public void executeScrapping(boolean needToContinue) throws SearchRequestUnsetException {
        Runtime.getRuntime().addShutdownHook(new Thread(this::end));
        view.showFrameWithInfo(ViewFrame.INFO, Message.BEGINNING_OF_EXECUTION);
        mainThread = Thread.currentThread();
        try {
            checkSearchConfiguration();
            if (!needToContinue) {
                ConfigurationHelper.reset(configHolder.getCCs());
                ConfigurationLoader.refresh(configHolder.getCCs());
            } else {
                ConfigurationHelper.analyzeIssues(configHolder.getCCs());
            }

            updaterService.startService();

            scrap();
        } catch (InterruptedException e) {
            handler.errorOccurred(e, null);
        } finally {
            end();
        }
    }

    private void checkSearchConfiguration() throws SearchRequestUnsetException {
        if (!manageSearchRequest().checkFields()) {
            throw new SearchRequestUnsetException(Message.SEARCH_REQUEST_NOT_SET.toString());
        }
    }

    private void sumItUp() {
        LocalDateTime endDate = LocalDateTime.now();
        long executionTime = ChronoUnit.MINUTES.between(startDate,endDate);
        SimpleLogger.log(LoggingLevel.INFO,String.format(Message.EXECUTION_TIME.toString(),executionTime));
        updaterService.addPreviousRequest();
        updaterService.writeSummery(ConfigurationHelper.wrapIssues(configHolder.getCCs()));
    }

    boolean isEnded = false;

    public void end() {
        if (isEnded) return;

        isEnded = true;
        updaterService.addMeta();
        try {
            updaterService.joinService();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        sumItUp();

        ConfigurationLoader.storeConfiguration(configHolder.getCCs());

        view.showFrameWithInfo(ViewFrame.INFO, Message.SCRAPING_DONE,
                ApplicationConfiguration.getInstance().getProperty("basic.result.path") +
                ApplicationConfiguration.getInstance().getProperty("basic.name") + "/");

        view.finish();

        try {
            SeleniumHelper.endSession();
            SimpleLogger.close();
        } catch (Exception ignored) {}
    }

    private void continueScrapping() throws InterruptedException {
        SimpleLogger.log(LoggingLevel.INFO, Message.SECOND_LAP);
        view.showFrameWithInfo(ViewFrame.INFO, Message.SECOND_LAP);
        ConfigurationHelper.analyzeIssues(configHolder.getCCs());
        execute(true);
    }

    private void scrap() throws InterruptedException {
        execute(false);
        continueScrapping();
        view.showFrameWithInfo(ViewFrame.INFO, Message.DUMP);
    }

    private void execute(Boolean ignoreInactive) throws InterruptedException {
        List<CourtConfiguration> mainCCS = configHolder.getCCs().stream().filter(x -> !x.isSingleStrategy() &&
                x.getStrategyName() != StrategyName.END_STRATEGY)
                .sorted((o1, o2) -> {
                    if (o1.getStrategyName() == StrategyName.MOSGORSUD_STRATEGY) return -1;
                    if (o2.getStrategyName() == StrategyName.MOSGORSUD_STRATEGY) return 1;
                    return 0;
                })
                .collect(Collectors.toList());

        List<CourtConfiguration> singleCCS = configHolder.getCCs().stream().filter(x -> x.getStrategyName()
                == StrategyName.CAPTCHA_STRATEGY).collect(Collectors.toList());

        singleCCS.addAll(configHolder.getCCs().stream()
                .filter(x -> x.isSingleStrategy() && x.getStrategyName() != StrategyName.CAPTCHA_STRATEGY
                        && x.getStrategyName() != StrategyName.END_STRATEGY && x.getStrategyName() != StrategyName.MOSGORSUD_STRATEGY).toList());

        if (ignoreInactive) {
            mainCCS = mainCCS.stream().filter(x -> x.getIssue() != Issue.INACTIVE_COURT
                    && x.getIssue() != Issue.INACTIVE_MODULE).collect(Collectors.toList());
            singleCCS = singleCCS.stream().filter(x -> x.getIssue() != Issue.INACTIVE_COURT
                    && x.getIssue() != Issue.INACTIVE_MODULE).collect(Collectors.toList());
        }

        if (selectedRegions != null) {
            mainCCS = mainCCS.stream().filter(x -> Arrays.stream(selectedRegions).anyMatch(r -> r == x.getRegion())).collect(Collectors.toList());
            singleCCS = singleCCS.stream().filter(x -> Arrays.stream(selectedRegions).anyMatch(r -> r == x.getRegion())).collect(Collectors.toList());
        }

        if (levels != null) {
            mainCCS = mainCCS.stream().filter(x->Arrays.stream(levels).anyMatch(r->r==x.getLevel())).collect(Collectors.toList());
            singleCCS = singleCCS.stream().filter(x->Arrays.stream(levels).anyMatch(r->r==x.getLevel())).collect(Collectors.toList());
        }

        if (strategyNames != null) {
            mainCCS = mainCCS.stream().filter(x->Arrays.stream(strategyNames).anyMatch(r->r==x.getStrategyName())).collect(Collectors.toList());
            singleCCS = singleCCS.stream().filter(x->Arrays.stream(strategyNames).anyMatch(r->r==x.getStrategyName())).collect(Collectors.toList());
        }

        mainCCS = checkIfNothingToExecute(mainCCS);
        singleCCS = checkIfNothingToExecute(singleCCS);


        countDownLatch = new CountDownLatch(mainCCS.size() + singleCCS.size());

        ThreadPoolExecutor seleniumExecutor = new StrategyThreadPoolExecutor(1, 1,
                10, TimeUnit.MINUTES, new ArrayBlockingQueue<>(singleCCS.size()), this);
        ThreadPoolExecutor mainExecutor = new StrategyThreadPoolExecutor(2, 4,
                10, TimeUnit.MINUTES, new ArrayBlockingQueue<>(mainCCS.size()), this);

        courts = mainCCS.size() + singleCCS.size();

        for (CourtConfiguration cc : singleCCS) {
            seleniumExecutor.execute(this.selectStrategy(cc));
        }

        for (CourtConfiguration cc : mainCCS) {
            mainExecutor.execute(this.selectStrategy(cc));
        }

        countDownLatch.await();

        mainExecutor.shutdown();
        seleniumExecutor.shutdown();
    }

    private synchronized void update(Collection<Case> cases) {
        countDownLatch.countDown();

        this.cases += cases.size();

        view.showFrameWithInfo(ViewFrame.INFO, Message.RESULT,
                String.valueOf(courts - countDownLatch.getCount()), String.valueOf(courts),
                String.valueOf(this.cases));

        refresh();

        if (!cases.isEmpty()) {
            updaterService.update(cases);
        }
    }

    private synchronized void refresh() {
        ConfigurationLoader.refresh(configHolder.getCCs());
    }


    private List<CourtConfiguration> checkIfNothingToExecute(List<CourtConfiguration> ccs) {
        if (ccs.isEmpty()) {
            CourtConfiguration emptyCC = new CourtConfiguration();
            emptyCC.setStrategyName(StrategyName.END_STRATEGY);
            ccs = new ArrayList<>();
            ccs.add(emptyCC);
            return ccs;
        }
        return ccs;
    }

    private SUDRFStrategy selectStrategy(CourtConfiguration cc) {
        switch (cc.getStrategyName()) {
            case PRIMARY_STRATEGY -> {
                return new PrimaryStrategy(cc);
            }
            case CAPTCHA_STRATEGY -> {
                return new CaptchaStrategy(cc);
            }
            case END_STRATEGY -> {
                return new EndStrategy(cc);
            }
            case MOSGORSUD_STRATEGY -> {
                return new MosGorSudStrategy(cc);
            }
            default -> throw new IllegalArgumentException(Message.STRATEGY_NOT_CHOSEN.toString());
        }
    }

}
