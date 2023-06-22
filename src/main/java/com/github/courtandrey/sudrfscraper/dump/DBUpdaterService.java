package com.github.courtandrey.sudrfscraper.dump;

import com.github.courtandrey.sudrfscraper.configuration.dumpconfiguration.ServerConnectionInfo;
import com.github.courtandrey.sudrfscraper.controller.ErrorHandler;
import com.github.courtandrey.sudrfscraper.dump.model.Case;
import com.github.courtandrey.sudrfscraper.dump.repository.Cases;
import com.github.courtandrey.sudrfscraper.service.ThreadHelper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.github.courtandrey.sudrfscraper.service.Constant.DB_Driver;

public class DBUpdaterService extends UpdaterService {
    private final CasesDB casesDB;
    public static class CasesDB{
        private final Cases cases;

        public static Connection getConnection() throws SQLException {
            ServerConnectionInfo connectionInfo = ServerConnectionInfo.getInstance();
            return DriverManager.getConnection(connectionInfo.getDbUrl(),connectionInfo.getUser(),
                    connectionInfo.getPassword());
        }

        public CasesDB(String name) throws ClassNotFoundException, SQLException {
            Class.forName(DB_Driver.toString());
            cases = new Cases(name);
        }

        protected void update(Case _case) {
            if (_case != null) cases.addCase(_case);
        }
    }

    public DBUpdaterService(String name, ErrorHandler handler) throws IOException, ClassNotFoundException, SQLException {
        super(name, handler);
        casesDB = new CasesDB(name);
    }

    @Override
    public void createMeta() throws IOException {
        writeMeta(getBasicProperties());
    }

    @Override
    public void run() {
        try {
            casesDB.cases.createTable();
            while (!isScrappingOver) {
                if (cases.isEmpty()) {
                    ThreadHelper.sleep(10);
                } else {
                    Case _case;
                    synchronized (this) {
                        _case = cases.poll();
                    }
                    casesDB.update(_case);
                }
            }
            while (!cases.isEmpty()) {
                Case _case = cases.poll();
                casesDB.update(_case);
            }
        }
        catch (SQLException e) {
            handler.errorOccurred(e, this);
        }
        finally {
            try {
                afterExecute();
            } catch (IOException e) {
                handler.errorOccurred(e, this);
            }
        }
    }

    public void close() {
        casesDB.cases.close();
    }
}
