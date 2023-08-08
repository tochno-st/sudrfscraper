package com.github.courtandrey.sudrfscraper.configuration.dumpconfiguration;

import com.github.courtandrey.sudrfscraper.configuration.ApplicationConfiguration;
import com.github.courtandrey.sudrfscraper.dump.DBUpdaterService;
import lombok.Getter;

import java.sql.SQLException;
public class ServerConnectionInfo {
    private ServerConnectionInfo() {
        ApplicationConfiguration configuration = ApplicationConfiguration.getInstance();
        this.DB_URL = configuration.getProperty("sql.url");
        this.user = configuration.getProperty("sql.usr");
        this.password = configuration.getProperty("sql.password");
    }

    private static ServerConnectionInfo instance;

    public static ServerConnectionInfo getInstance() {
        if (instance == null) {
            instance = new ServerConnectionInfo();
        }
        return instance;
    }

    private String DB_URL;
    @Getter
    private String user;
    @Getter
    private String password;
    @Getter
    private boolean remember;

    public static void setInstance(ServerConnectionInfo instance) {
        ServerConnectionInfo.instance = instance;
    }

    public void setRemember(boolean remember) {
        this.remember = remember;
    }

    public String getDbUrl() {
        return DB_URL;
    }

    public void setDbUrl(String dbUrl) {
        this.DB_URL = dbUrl;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void testConnection() throws SQLException {
        if (!DB_URL.isEmpty()) {
            DBUpdaterService.CasesDB.getConnection();
        }
        else {
            throw new SQLException();
        }
    }
}
