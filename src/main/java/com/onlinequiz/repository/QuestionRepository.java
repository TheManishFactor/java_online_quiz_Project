package com.onlinequiz.repository;

import com.onlinequiz.database.DatabaseConnection;
import com.onlinequiz.model.Question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite persistence for questions. Insert and delete keep the cached
 * {@code quizzes.total_questions} column in sync inside one transaction.
 */
public class QuestionRepository {

    private static final String SELECT_COLUMNS =
            "id, quiz_id, question_text, option_a, option_b, option_c, option_d,"
                    + " correct_answer";

    private final String databaseFile;

    public QuestionRepository() {
        this(DatabaseConnection.DEFAULT_DATABASE_FILE);
    }

    public QuestionRepository(String databaseFile) {
        this.databaseFile = databaseFile;
    }

    public Question addQuestion(Question question) throws SQLException {
        String sql = "INSERT INTO questions(quiz_id, question_text, option_a, option_b,"
                + " option_c, option_d, correct_answer) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    bindQuestion(statement, question);
                    JdbcSupport.requireSingleRow(statement.executeUpdate(), "Question insert");
                }
                int questionId = JdbcSupport.lastInsertId(connection);
                refreshQuestionCount(connection, question.getQuizId());
                connection.commit();
                question.setId(questionId);
                return question;
            } catch (SQLException error) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    error.addSuppressed(rollbackError);
                }
                throw error;
            }
        }
    }

    public List<Question> getQuestionsByQuizId(int quizId) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM questions"
                + " WHERE quiz_id = ? ORDER BY id";
        List<Question> questions = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quizId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    questions.add(mapQuestion(resultSet));
                }
            }
        }
        return questions;
    }

    public Question getQuestionById(int questionId) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM questions WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, questionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapQuestion(resultSet) : null;
            }
        }
    }

    public boolean updateQuestion(Question question) throws SQLException {
        String sql = "UPDATE questions SET question_text = ?, option_a = ?, option_b = ?,"
                + " option_c = ?, option_d = ?, correct_answer = ? WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, question.getQuestionText());
            statement.setString(2, question.getOptionA());
            statement.setString(3, question.getOptionB());
            statement.setString(4, question.getOptionC());
            statement.setString(5, question.getOptionD());
            statement.setString(6, question.getCorrectAnswer());
            statement.setInt(7, question.getId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteQuestion(int questionId) throws SQLException {
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            try {
                Integer quizId = findQuizId(connection, questionId);
                if (quizId == null) {
                    connection.rollback();
                    return false;
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM questions WHERE id = ?")) {
                    statement.setInt(1, questionId);
                    if (statement.executeUpdate() == 0) {
                        connection.rollback();
                        return false;
                    }
                }
                refreshQuestionCount(connection, quizId);
                connection.commit();
                return true;
            } catch (SQLException error) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    error.addSuppressed(rollbackError);
                }
                throw error;
            }
        }
    }

    private Connection openConnection() throws SQLException {
        return DatabaseConnection.getConnection(databaseFile);
    }

    private Integer findQuizId(Connection connection, int questionId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT quiz_id FROM questions WHERE id = ?")) {
            statement.setInt(1, questionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : null;
            }
        }
    }

    private void refreshQuestionCount(Connection connection, int quizId) throws SQLException {
        String sql = "UPDATE quizzes SET total_questions ="
                + " (SELECT COUNT(*) FROM questions WHERE quiz_id = ?) WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quizId);
            statement.setInt(2, quizId);
            statement.executeUpdate();
        }
    }

    private void bindQuestion(PreparedStatement statement, Question question)
            throws SQLException {
        statement.setInt(1, question.getQuizId());
        statement.setString(2, question.getQuestionText());
        statement.setString(3, question.getOptionA());
        statement.setString(4, question.getOptionB());
        statement.setString(5, question.getOptionC());
        statement.setString(6, question.getOptionD());
        statement.setString(7, question.getCorrectAnswer());
    }

    private Question mapQuestion(ResultSet resultSet) throws SQLException {
        return new Question(
                resultSet.getInt("id"),
                resultSet.getInt("quiz_id"),
                resultSet.getString("question_text"),
                resultSet.getString("option_a"),
                resultSet.getString("option_b"),
                resultSet.getString("option_c"),
                resultSet.getString("option_d"),
                resultSet.getString("correct_answer"));
    }
}
