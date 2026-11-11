package com.onlinequiz.util;

import com.onlinequiz.database.DatabaseConnection;
import com.onlinequiz.database.DatabaseInitializer;
import com.onlinequiz.exception.InvalidLoginException;
import com.onlinequiz.exception.InvalidQuestionException;
import com.onlinequiz.exception.QuizNotFoundException;
import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.model.Question;
import com.onlinequiz.model.Quiz;
import com.onlinequiz.model.Result;
import com.onlinequiz.model.Student;
import com.onlinequiz.model.User;
import com.onlinequiz.repository.UserRepository;
import com.onlinequiz.service.QuestionService;
import com.onlinequiz.service.QuizService;
import com.onlinequiz.service.ResultService;
import com.onlinequiz.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * End-to-end checks for the Phase 4 database layer. The test uses its own file
 * under {@code target/test-databases}, deletes it before every run and never
 * touches {@code online_quiz.db}.
 *
 * Run with:
 * java -cp "target/classes;target/test-classes;<dependency-classpath>" com.onlinequiz.util.DatabaseTest
 */
public final class DatabaseTest {

    private static final String TEST_DATABASE = "target/test-databases/online_quiz_test.db";
    private static final String TEST_EMAIL = "asha.phase4@test.com";
    private static final String TEST_PASSWORD = "secret-pass";

    private static int checks;
    private static int failures;

    private static UserService userService;
    private static QuizService quizService;
    private static QuestionService questionService;
    private static ResultService resultService;

    private DatabaseTest() {
    }

    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println(" PHASE 4 - DATABASE TESTS");
        System.out.println("=====================================================");

        try {
            prepareTestDatabase();
            testConnection();
            testTableInitialization();

            userService = new UserService(TEST_DATABASE);
            quizService = new QuizService(TEST_DATABASE);
            questionService = new QuestionService(TEST_DATABASE);
            resultService = new ResultService(TEST_DATABASE);

            Student student = new Student("Asha Rao", TEST_EMAIL, TEST_PASSWORD);
            testUserRegistration(student);
            testDuplicateEmailRejection(student);
            testUserRetrieval(student);

            Quiz quiz = testQuizCreation();
            testQuizRetrieval(quiz);
            Question question = testQuestionInsertion(quiz);
            testQuestionRetrieval(quiz, question);
            testQuizUpdate(quiz);

            Result result = testResultInsertion(student, quiz);
            testResultRetrieval(student, quiz, result);
            testForeignKeyConstraint();
            testRecordDeletion(student, quiz, question);
        } catch (Exception error) {
            System.err.println("DATABASE TESTS ABORTED: " + error.getMessage());
            error.printStackTrace();
            System.exit(1);
        }

        System.out.println();
        if (failures == 0) {
            System.out.println("ALL DATABASE CHECKS PASSED (" + checks + " checks)");
        } else {
            System.out.println(failures + " OF " + checks + " DATABASE CHECKS FAILED");
            System.exit(1);
        }
    }

    private static void testConnection() {
        check("Database connection opens and executes SELECT 1", () -> {
            try (Connection connection = DatabaseConnection.getConnection(TEST_DATABASE);
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT 1")) {
                require(resultSet.next(), "SELECT 1 returned no row");
                require(resultSet.getInt(1) == 1, "SELECT 1 returned the wrong value");
            }
        });
    }

    private static void testTableInitialization() {
        check("All four tables and sample quizzes initialize", () -> {
            String[] tables = {"users", "quizzes", "questions", "results"};
            try (Connection connection = DatabaseConnection.getConnection(TEST_DATABASE)) {
                for (String table : tables) {
                    require(tableExists(connection, table), "Missing table: " + table);
                }
                require(countRows(connection, "quizzes") >= 2,
                        "Sample quizzes were not seeded");
            }
        });

        check("Foreign-key enforcement is enabled on connections", () -> {
            try (Connection connection = DatabaseConnection.getConnection(TEST_DATABASE);
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("PRAGMA foreign_keys")) {
                require(resultSet.next() && resultSet.getInt(1) == 1,
                        "PRAGMA foreign_keys should return 1");
            }
        });

        check("Initialization is idempotent and does not duplicate sample quizzes", () -> {
            long before;
            long after;
            try (Connection connection = DatabaseConnection.getConnection(TEST_DATABASE)) {
                before = countRows(connection, "quizzes");
            }
            DatabaseInitializer.initialize(TEST_DATABASE);
            try (Connection connection = DatabaseConnection.getConnection(TEST_DATABASE)) {
                after = countRows(connection, "quizzes");
            }
            require(before == after, "Sample quizzes were inserted twice");
        });
    }

    private static void testUserRegistration(Student student) {
        check("User registration stores a PBKDF2 hash", () -> {
            require(userService.registerUser(student), "Registration should return true");
            require(student.getId() > 0, "Registered user should receive an id");
            require(student.getPasswordHash().startsWith("pbkdf2$"),
                    "Password hash should use the PBKDF2 format");
            require(!student.getPasswordHash().contains(TEST_PASSWORD),
                    "Plain password must not appear in the stored hash");
        });

        expectFailure(ValidationException.class,
                "Repository rejects a plaintext password before SQL runs",
                () -> new UserRepository(TEST_DATABASE).registerUser(
                        new Student("Plain Text", "plaintext.phase4@test.com",
                                "plain-password")));
    }

    private static void testDuplicateEmailRejection(Student student) {
        expectFailure(ValidationException.class, "Duplicate email is rejected",
                () -> userService.registerUser(
                        new Student("Copy Cat", TEST_EMAIL, "another-password")));
    }

    private static void testUserRetrieval(Student student) {
        check("User retrieval returns the stored account", () -> {
            User loaded = userService.findUserById(student.getId());
            require(loaded != null, "User should be retrievable by id");
            require(TEST_EMAIL.equals(loaded.getEmail()), "Stored email does not match");
            require(User.ROLE_STUDENT.equals(loaded.getRole()), "Role should be STUDENT");

            User byEmail = userService.findUserByEmail(TEST_EMAIL);
            require(byEmail != null && byEmail.getId() == student.getId(),
                    "User should be retrievable by email");

            User loggedIn = userService.loginUser(TEST_EMAIL.toUpperCase(), TEST_PASSWORD);
            require(loggedIn.getId() == student.getId(), "Login returned the wrong user");
            require(userService.getActiveSessionIds().contains(student.getId()),
                    "Active session was not recorded");
            require(userService.logoutUser(student.getId()), "Logout should end the session");
        });

        expectFailure(InvalidLoginException.class, "Wrong password is rejected by login",
                () -> userService.loginUser(TEST_EMAIL, "wrong-password"));
    }

    private static Quiz testQuizCreation() {
        Quiz quiz = new Quiz("Phase 4 Quiz", "Programming", 12);
        check("Quiz creation assigns a database id", () -> {
            require(quizService.addQuiz(quiz), "Quiz creation should return true");
            require(quiz.getId() > 0, "Created quiz should receive an id");
        });
        return quiz;
    }

    private static void testQuizRetrieval(Quiz quiz) {
        check("Quiz retrieval by id returns the stored quiz", () -> {
            Quiz loaded = quizService.getQuizById(quiz.getId());
            require("Phase 4 Quiz".equals(loaded.getTitle()), "Title does not match");
            require("Programming".equals(loaded.getCategory()), "Category does not match");
            require(loaded.getDuration() == 12, "Duration does not match");
            require(loaded.getQuestions().isEmpty(), "New quiz should have no questions");
        });
    }

    private static Question testQuestionInsertion(Quiz quiz) {
        Question question = new Question(quiz.getId(),
                "Which keyword is used to inherit a class?",
                "this", "super", "extends", "implements", "C");
        check("Question insertion assigns an id and updates the quiz count", () -> {
            questionService.addQuestion(question);
            require(question.getId() > 0, "Inserted question should receive an id");
            Quiz refreshed = quizService.getQuizById(quiz.getId());
            require(refreshed.getTotalQuestions() == 1,
                    "Quiz total_questions should be 1");
        });
        return question;
    }

    private static void testQuestionRetrieval(Quiz quiz, Question question) {
        check("Question retrieval by quiz id returns the inserted question", () -> {
            List<Question> questions = questionService.getQuestionsByQuizId(quiz.getId());
            require(questions.size() == 1, "Expected exactly one question");
            require("Which keyword is used to inherit a class?".equals(
                    questions.get(0).getQuestionText()), "Question text does not match");
            require("C".equals(questions.get(0).getCorrectAnswer()),
                    "Correct answer does not match");

            Question byId = questionService.getQuestionById(question.getId());
            require(byId.getQuizId() == quiz.getId(), "Question quiz id does not match");
        });
    }

    private static void testQuizUpdate(Quiz quiz) {
        check("Quiz update persists title, duration and question count", () -> {
            Quiz stored = quizService.getQuizById(quiz.getId());
            stored.setTitle("Phase 4 Quiz Updated");
            stored.setDuration(20);

            require(quizService.updateQuiz(stored), "Update should return true");
            Quiz updated = quizService.getQuizById(quiz.getId());
            require("Phase 4 Quiz Updated".equals(updated.getTitle()), "Title was not updated");
            require(updated.getDuration() == 20, "Duration was not updated");
            require(updated.getTotalQuestions() == 1, "Question count should stay 1");
        });
    }

    private static Result testResultInsertion(Student student, Quiz quiz) {
        Result result = new Result(student.getId(), quiz.getId(), 1, 1, "2026-11-11");
        check("Result insertion assigns a database id", () -> {
            Result saved = resultService.saveResult(result);
            require(saved.getId() > 0, "Saved result should receive an id");
            require(saved.getPercentage() == 100.0, "Percentage should be 100");
        });
        return result;
    }

    private static void testResultRetrieval(Student student, Quiz quiz, Result result) {
        check("Result retrieval works by id, student, quiz and grouped map", () -> {
            Result byId = resultService.getResultById(result.getId());
            require(byId != null, "Result should be retrievable by id");
            require(byId.getScore() == 1, "Stored score does not match");

            require(resultService.getResultsByStudentId(student.getId()).size() == 1,
                    "Student should have one result");
            require(resultService.getResultsByQuizId(quiz.getId()).size() == 1,
                    "Quiz should have one result");

            Map<Integer, List<Result>> grouped = resultService.groupResultsByStudent();
            require(grouped.containsKey(student.getId()), "Grouped map should contain the student");
            require(grouped.get(student.getId()).size() == 1,
                    "Grouped map should contain one result");
        });
    }

    private static void testForeignKeyConstraint() {
        expectFailure(SQLException.class, "Foreign key rejects a question for an unknown quiz",
                () -> {
                    String sql = "INSERT INTO questions(quiz_id, question_text, option_a,"
                            + " option_b, option_c, option_d, correct_answer)"
                            + " VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (Connection connection = DatabaseConnection.getConnection(TEST_DATABASE);
                         PreparedStatement statement = connection.prepareStatement(sql)) {
                        statement.setInt(1, 999_999);
                        statement.setString(2, "Orphan question?");
                        statement.setString(3, "A");
                        statement.setString(4, "B");
                        statement.setString(5, "C");
                        statement.setString(6, "D");
                        statement.setString(7, "A");
                        statement.executeUpdate();
                    }
                });
    }

    private static void testRecordDeletion(Student student, Quiz quiz, Question question) {
        check("Question deletion updates the quiz count", () -> {
            require(questionService.deleteQuestion(question.getId()),
                    "Question deletion should return true");
            Quiz refreshed = quizService.getQuizById(quiz.getId());
            require(refreshed.getTotalQuestions() == 0, "Question count should return to 0");
        });

        expectFailure(InvalidQuestionException.class, "Deleted question is no longer found",
                () -> questionService.getQuestionById(question.getId()));

        check("Quiz deletion cascades results", () -> {
            require(quizService.deleteQuiz(quiz.getId()), "Quiz deletion should return true");
            require(resultService.getResultsByStudentId(student.getId()).isEmpty(),
                    "Results should be removed by the cascade");
        });

        expectFailure(QuizNotFoundException.class, "Deleted quiz can no longer be loaded",
                () -> quizService.getQuizById(quiz.getId()));

        check("User deletion removes the account", () -> {
            require(userService.deleteUser(student.getId()), "User deletion should return true");
            require(userService.findUserById(student.getId()) == null,
                    "Deleted user should not be found");
        });
    }

    private static void prepareTestDatabase() throws IOException, SQLException {
        Files.createDirectories(Paths.get("target/test-databases"));
        deleteIfPresent(TEST_DATABASE);
        deleteIfPresent(TEST_DATABASE + "-journal");
        deleteIfPresent(TEST_DATABASE + "-wal");
        deleteIfPresent(TEST_DATABASE + "-shm");
        DatabaseInitializer.initialize(TEST_DATABASE);
    }

    private static void deleteIfPresent(String file) throws IOException {
        Files.deleteIfExists(Paths.get(file));
    }

    private static boolean tableExists(Connection connection, String table) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?")) {
            statement.setString(1, table);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static long countRows(Connection connection, String table) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return resultSet.next() ? resultSet.getLong(1) : 0;
        }
    }

    private static void check(String description, Action action) {
        try {
            action.run();
            pass(description);
        } catch (Throwable error) {
            fail(description, "unexpected " + error.getClass().getSimpleName()
                    + ": " + error.getMessage());
        }
    }

    private static <T extends Throwable> void expectFailure(Class<T> expected,
                                                            String description,
                                                            Action action) {
        try {
            action.run();
            fail(description, "expected " + expected.getSimpleName() + " but nothing was thrown");
        } catch (Throwable error) {
            if (expected.isInstance(error)) {
                pass(description + " -> " + error.getClass().getSimpleName()
                        + ": " + error.getMessage());
            } else {
                fail(description, "expected " + expected.getSimpleName() + " but got "
                        + error.getClass().getSimpleName());
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void pass(String description) {
        checks++;
        System.out.println("[PASS] " + description);
    }

    private static void fail(String description, String reason) {
        checks++;
        failures++;
        System.out.println("[FAIL] " + description + " (" + reason + ")");
    }

    @FunctionalInterface
    private interface Action {
        void run() throws Exception;
    }
}
