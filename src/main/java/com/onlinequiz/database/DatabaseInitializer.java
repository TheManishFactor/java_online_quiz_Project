package com.onlinequiz.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates the application schema and inserts sample quizzes the first time the
 * database is used. Every statement is idempotent, so calling this on each start
 * is safe.
 */
public final class DatabaseInitializer {

    private static final String CREATE_USERS =
            "CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT NOT NULL,"
                    + "email TEXT NOT NULL UNIQUE,"
                    + "password_hash TEXT NOT NULL,"
                    + "role TEXT NOT NULL CHECK (role IN ('STUDENT', 'ADMIN'))"
                    + ")";

    private static final String CREATE_QUIZZES =
            "CREATE TABLE IF NOT EXISTS quizzes ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "title TEXT NOT NULL,"
                    + "category TEXT NOT NULL,"
                    + "duration INTEGER NOT NULL CHECK (duration >= 1),"
                    + "total_questions INTEGER NOT NULL DEFAULT 0"
                    + " CHECK (total_questions >= 0)"
                    + ")";

    private static final String CREATE_QUESTIONS =
            "CREATE TABLE IF NOT EXISTS questions ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "quiz_id INTEGER NOT NULL,"
                    + "question_text TEXT NOT NULL,"
                    + "option_a TEXT NOT NULL,"
                    + "option_b TEXT NOT NULL,"
                    + "option_c TEXT NOT NULL,"
                    + "option_d TEXT NOT NULL,"
                    + "correct_answer TEXT NOT NULL"
                    + " CHECK (correct_answer IN ('A', 'B', 'C', 'D')),"
                    + "FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE"
                    + ")";

    private static final String CREATE_RESULTS =
            "CREATE TABLE IF NOT EXISTS results ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "student_id INTEGER NOT NULL,"
                    + "quiz_id INTEGER NOT NULL,"
                    + "score INTEGER NOT NULL CHECK (score >= 0),"
                    + "total_questions INTEGER NOT NULL CHECK (total_questions > 0),"
                    + "percentage REAL NOT NULL"
                    + " CHECK (percentage >= 0 AND percentage <= 100),"
                    + "attempt_date TEXT NOT NULL,"
                    + "CHECK (score <= total_questions),"
                    + "FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,"
                    + "FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE"
                    + ")";

    private DatabaseInitializer() {
    }

    /**
     * Initializes the default application database.
     */
    public static void initialize() throws SQLException {
        initialize(DatabaseConnection.DEFAULT_DATABASE_FILE);
    }

    /**
     * Initializes a database file, creating it when necessary.
     */
    public static void initialize(String databaseFile) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection(databaseFile)) {
            initialize(connection);
        }
    }

    /**
     * Creates tables, constraints and indexes, then seeds sample data only if the
     * quiz table is empty.
     */
    public static void initialize(Connection connection) throws SQLException {
        if (connection == null) {
            throw new SQLException("A database connection is required");
        }
        createTables(connection);
        seedSampleData(connection);
    }

    private static void createTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_USERS);
            statement.executeUpdate(CREATE_QUIZZES);
            statement.executeUpdate(CREATE_QUESTIONS);
            statement.executeUpdate(CREATE_RESULTS);

            statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_questions_quiz_id"
                            + " ON questions(quiz_id)");
            statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_quizzes_category"
                            + " ON quizzes(category)");
            statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_results_student_id"
                            + " ON results(student_id)");
            statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_results_quiz_id"
                            + " ON results(quiz_id)");
        }
    }

    private static void seedSampleData(Connection connection) throws SQLException {
        if (countRows(connection, "quizzes") > 0) {
            return;
        }

        connection.setAutoCommit(false);
        try {
            int javaQuizId = insertQuiz(connection, "Java Basics", "Programming", 10);
            insertQuestion(connection, javaQuizId,
                    "Which keyword is used to inherit a class in Java?",
                    "this", "super", "extends", "implements", "C");
            insertQuestion(connection, javaQuizId,
                    "Which collection allows duplicate elements?",
                    "HashSet", "ArrayList", "TreeSet", "HashMap", "B");
            insertQuestion(connection, javaQuizId,
                    "Which method starts a thread?",
                    "run()", "start()", "execute()", "begin()", "B");

            int capitalsQuizId = insertQuiz(connection, "World Capitals", "General Knowledge", 8);
            insertQuestion(connection, capitalsQuizId,
                    "What is the capital of Japan?",
                    "Seoul", "Beijing", "Tokyo", "Bangkok", "C");
            insertQuestion(connection, capitalsQuizId,
                    "Which river flows through Paris?",
                    "Thames", "Seine", "Danube", "Rhine", "B");
            insertQuestion(connection, capitalsQuizId,
                    "Which country has the city of Marrakesh?",
                    "Morocco", "Egypt", "Tunisia", "Algeria", "A");

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException error) {
            try {
                connection.rollback();
            } catch (SQLException rollbackError) {
                error.addSuppressed(rollbackError);
            }
            throw error;
        }
    }

    private static int insertQuiz(Connection connection, String title, String category,
                                  int duration) throws SQLException {
        String sql = "INSERT INTO quizzes(title, category, duration, total_questions)"
                + " VALUES (?, ?, ?, 0)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title.trim());
            statement.setString(2, category.trim());
            statement.setInt(3, duration);
            statement.executeUpdate();
        }
        return lastInsertId(connection);
    }

    private static void insertQuestion(Connection connection, int quizId, String text,
                                       String optionA, String optionB, String optionC,
                                       String optionD, String correctAnswer)
            throws SQLException {
        String sql = "INSERT INTO questions(quiz_id, question_text, option_a, option_b,"
                + " option_c, option_d, correct_answer) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quizId);
            statement.setString(2, text);
            statement.setString(3, optionA);
            statement.setString(4, optionB);
            statement.setString(5, optionC);
            statement.setString(6, optionD);
            statement.setString(7, correctAnswer);
            statement.executeUpdate();
        }
        updateQuestionCount(connection, quizId);
    }

    private static void updateQuestionCount(Connection connection, int quizId)
            throws SQLException {
        String sql = "UPDATE quizzes SET total_questions ="
                + " (SELECT COUNT(*) FROM questions WHERE quiz_id = ?) WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quizId);
            statement.setInt(2, quizId);
            statement.executeUpdate();
        }
    }

    private static int countRows(Connection connection, String table) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private static int lastInsertId(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT last_insert_rowid()")) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }
}
