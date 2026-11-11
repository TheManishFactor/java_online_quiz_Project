package com.onlinequiz.repository;

import com.onlinequiz.database.DatabaseConnection;
import com.onlinequiz.model.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite persistence for quiz attempts.
 */
public class ResultRepository {

    private static final String SELECT_COLUMNS =
            "id, student_id, quiz_id, score, total_questions, percentage, attempt_date";

    private final String databaseFile;

    public ResultRepository() {
        this(DatabaseConnection.DEFAULT_DATABASE_FILE);
    }

    public ResultRepository(String databaseFile) {
        this.databaseFile = databaseFile;
    }

    public Result saveResult(Result result) throws SQLException {
        String sql = "INSERT INTO results(student_id, quiz_id, score, total_questions,"
                + " percentage, attempt_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, result.getUserId());
            statement.setInt(2, result.getQuizId());
            statement.setInt(3, result.getScore());
            statement.setInt(4, result.getTotalQuestions());
            statement.setDouble(5, result.getPercentage());
            statement.setString(6, result.getAttemptDate());

            JdbcSupport.requireSingleRow(statement.executeUpdate(), "Result insert");
            result.setId(JdbcSupport.lastInsertId(connection));
            return result;
        }
    }

    public Result getResultById(int resultId) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM results WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, resultId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapResult(resultSet) : null;
            }
        }
    }

    public List<Result> getResultsByStudentId(int studentId) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM results"
                + " WHERE student_id = ? ORDER BY id";
        return queryResults(sql, studentId);
    }

    public List<Result> getResultsByQuizId(int quizId) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM results"
                + " WHERE quiz_id = ? ORDER BY id";
        return queryResults(sql, quizId);
    }

    public List<Result> getAllResults() throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM results ORDER BY id";
        List<Result> results = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                results.add(mapResult(resultSet));
            }
        }
        return results;
    }

    private Connection openConnection() throws SQLException {
        return DatabaseConnection.getConnection(databaseFile);
    }

    private List<Result> queryResults(String sql, int parameter) throws SQLException {
        List<Result> results = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, parameter);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapResult(resultSet));
                }
            }
        }
        return results;
    }

    private Result mapResult(ResultSet resultSet) throws SQLException {
        return new Result(
                resultSet.getInt("id"),
                resultSet.getInt("student_id"),
                resultSet.getInt("quiz_id"),
                resultSet.getInt("score"),
                resultSet.getInt("total_questions"),
                resultSet.getDouble("percentage"),
                resultSet.getString("attempt_date"));
    }
}
