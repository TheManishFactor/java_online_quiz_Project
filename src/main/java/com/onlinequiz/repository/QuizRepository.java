package com.onlinequiz.repository;

import com.onlinequiz.database.DatabaseConnection;
import com.onlinequiz.model.Quiz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite persistence for quizzes. Questions are loaded separately by
 * {@link QuestionRepository}; the service layer joins the two views.
 */
public class QuizRepository {

    private final String databaseFile;

    public QuizRepository() {
        this(DatabaseConnection.DEFAULT_DATABASE_FILE);
    }

    public QuizRepository(String databaseFile) {
        this.databaseFile = databaseFile;
    }

    public Quiz createQuiz(Quiz quiz) throws SQLException {
        String sql = "INSERT INTO quizzes(title, category, duration, total_questions)"
                + " VALUES (?, ?, ?, ?)";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, quiz.getTitle());
            statement.setString(2, quiz.getCategory());
            statement.setInt(3, quiz.getDuration());
            statement.setInt(4, quiz.getTotalQuestions());

            JdbcSupport.requireSingleRow(statement.executeUpdate(), "Quiz insert");
            quiz.setId(JdbcSupport.lastInsertId(connection));
            return quiz;
        }
    }

    public Quiz getQuizById(int quizId) throws SQLException {
        String sql = "SELECT id, title, category, duration, total_questions"
                + " FROM quizzes WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quizId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapQuiz(resultSet) : null;
            }
        }
    }

    public List<Quiz> getAllQuizzes() throws SQLException {
        String sql = "SELECT id, title, category, duration, total_questions"
                + " FROM quizzes ORDER BY id";
        List<Quiz> quizzes = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                quizzes.add(mapQuiz(resultSet));
            }
        }
        return quizzes;
    }

    public boolean updateQuiz(Quiz quiz) throws SQLException {
        String sql = "UPDATE quizzes SET title = ?, category = ?, duration = ?,"
                + " total_questions = ? WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, quiz.getTitle());
            statement.setString(2, quiz.getCategory());
            statement.setInt(3, quiz.getDuration());
            statement.setInt(4, quiz.getTotalQuestions());
            statement.setInt(5, quiz.getId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteQuiz(int quizId) throws SQLException {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM quizzes WHERE id = ?")) {
            statement.setInt(1, quizId);
            return statement.executeUpdate() > 0;
        }
    }

    private Connection openConnection() throws SQLException {
        return DatabaseConnection.getConnection(databaseFile);
    }

    private Quiz mapQuiz(ResultSet resultSet) throws SQLException {
        return new Quiz(
                resultSet.getInt("id"),
                resultSet.getString("title"),
                resultSet.getString("category"),
                resultSet.getInt("duration"),
                resultSet.getInt("total_questions"));
    }
}
