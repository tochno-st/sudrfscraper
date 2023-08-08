package com.github.courtandrey.sudrfscraper.dump.repository;

import com.github.courtandrey.sudrfscraper.dump.DBUpdaterService;
import com.github.courtandrey.sudrfscraper.dump.model.Case;
import com.github.courtandrey.sudrfscraper.service.logger.LoggingLevel;
import com.github.courtandrey.sudrfscraper.service.logger.Message;
import com.github.courtandrey.sudrfscraper.service.logger.SimpleLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class Cases {
    Connection connection;
    private final String name;

    public Cases(String name) throws SQLException {
        this.name = name;
    }

    private static final String create = "CREATE TABLE IF NOT EXISTS %s (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "region_id INT," +
            "court_name VARCHAR(255)," +
            "case_number VARCHAR(255)," +
            "entry_date VARCHAR(255)," +
            "names_articles VARCHAR(4095)," +
            "judge VARCHAR(255)," +
            "result_date VARCHAR(255)," +
            "decision VARCHAR(255)," +
            "end_date VARCHAR(255)," +
            "decision_text MEDIUMTEXT," +
            " UNIQUE KEY uni ((coalesce(case_number, ''))," +
            "(coalesce(decision, ''))," +
            "(coalesce(judge, ''))," +
            "(coalesce(court_name, '')))" +
            ");";
    private static final String preparedInsert = "INSERT INTO %s (region_id, court_name, " +
            "case_number, entry_date, names_articles, judge, result_date, decision, end_date, decision_text) VALUES" +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public void addCase(Case _case) {
        try {
            PreparedStatement query = connection.prepareStatement(String.format(preparedInsert,name));
            query.setInt(1,_case.getRegion());
            query.setString(2,_case.getName());
            query.setString(3,_case.getCaseNumber());
            query.setString(4, _case.getEntryDate());
            query.setString(5, _case.getNames());
            query.setString(6,_case.getJudge());
            query.setString(7,_case.getResultDate());
            query.setString(8,_case.getDecision());
            query.setString(9,_case.getEndDate());
            query.setString(10, _case.getText());
            query.execute();
        } catch (SQLException e) {
            SimpleLogger.log(LoggingLevel.WARNING, String.format(Message.SOME_SQL_EXCEPTION.toString(),e));
        }

    }

    public void createTable() throws SQLException {
        executeSqlStatement(String.format(create,name));
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void executeSqlStatement(String query) throws SQLException {
        reopenConnection();
        Statement statement = connection.createStatement();
        statement.execute(query);
        statement.close();
    }

    private void reopenConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DBUpdaterService.CasesDB.getConnection();
        }
    }
}
