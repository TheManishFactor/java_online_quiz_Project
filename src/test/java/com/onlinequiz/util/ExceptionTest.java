package com.onlinequiz.util;

import com.onlinequiz.exception.InvalidLoginException;
import com.onlinequiz.exception.InvalidQuestionException;
import com.onlinequiz.exception.QuizNotFoundException;
import com.onlinequiz.exception.QuizSubmissionException;
import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.model.Question;
import com.onlinequiz.model.Quiz;
import com.onlinequiz.model.Result;
import com.onlinequiz.model.Student;

import java.util.Collections;

/**
 * Console test for the Phase 3 exception handling. Every scenario prints PASS or
 * FAIL; the process exits with status 1 when any check fails.
 *
 * Run with:
 * java -cp "target/classes;target/test-classes" com.onlinequiz.util.ExceptionTest
 */
public final class ExceptionTest {

    private static int checks;
    private static int failures;

    private ExceptionTest() {
    }

    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println(" PHASE 3 - EXCEPTION HANDLING TESTS");
        System.out.println("=====================================================");

        testValidUserInformation();
        testInvalidUserInformation();
        testValidQuizRetrieval();
        testMissingQuizRetrieval();
        testValidQuestionCreation();
        testInvalidQuestionCreation();
        testValidQuizSubmission();
        testNegativeScore();
        testScoreGreaterThanTotal();
        testInvalidQuizSubmission();

        System.out.println();
        if (failures == 0) {
            System.out.println("ALL EXCEPTION CHECKS PASSED (" + checks + " checks)");
        } else {
            System.out.println(failures + " OF " + checks + " EXCEPTION CHECKS FAILED");
            System.exit(1);
        }
    }

    /** 1. A valid account and its credentials are accepted. */
    private static void testValidUserInformation() {
        check("Valid user information is accepted", () -> {
            Student student = new Student(1, "Asha Rao", "Asha@Example.com", "hash-123");
            require("asha@example.com".equals(student.getEmail()),
                    "Email should be normalized to lower case");
            require(student.authenticate("asha@example.com", "hash-123") == student,
                    "Valid credentials should return the same account");
        });
    }

    /** 2. Invalid account data fails fast with ValidationException. */
    private static void testInvalidUserInformation() {
        expectFailure(ValidationException.class, "Negative user id is rejected",
                () -> new Student(-1, "Asha Rao", "asha@example.com", "hash-123"));
        expectFailure(ValidationException.class, "Blank user name is rejected",
                () -> new Student(1, "   ", "asha@example.com", "hash-123"));
        expectFailure(ValidationException.class, "Malformed email is rejected",
                () -> new Student(1, "Asha Rao", "asha.example.com", "hash-123"));
        expectFailure(InvalidLoginException.class, "Wrong password is rejected",
                () -> new Student(1, "Asha Rao", "asha@example.com", "hash-123")
                        .authenticate("asha@example.com", "wrong-hash"));
    }

    /** 3. A quiz that exists in the collection is returned. */
    private static void testValidQuizRetrieval() {
        check("Valid quiz retrieval returns the matching quiz", () -> {
            Quiz quiz = buildQuiz();
            Quiz found = Quiz.findById(Collections.singletonList(quiz), 7);
            require(found == quiz, "findById should return the same quiz instance");
        });
    }

    /** 4. A missing quiz raises the checked QuizNotFoundException. */
    private static void testMissingQuizRetrieval() {
        expectFailure(QuizNotFoundException.class, "Unknown quiz id raises QuizNotFoundException",
                () -> Quiz.findById(Collections.singletonList(buildQuiz()), 404));
        expectFailure(QuizNotFoundException.class, "Missing quiz collection raises QuizNotFoundException",
                () -> Quiz.findById(null, 7));
    }

    /** 5. A well-formed question keeps its data and grades answers correctly. */
    private static void testValidQuestionCreation() {
        check("Valid question creation stores all options", () -> {
            Question question = new Question(7, "Which keyword inherits a class?",
                    "this", "super", "extends", "final", "C");
            require("extends".equals(question.getOption('C')), "Option C should be 'extends'");
            require(question.isCorrect("c"), "Answer checking should ignore case");
            require(!question.isCorrect("A"), "A wrong label must not be accepted");
        });
    }

    /** 6. Malformed question data raises InvalidQuestionException. */
    private static void testInvalidQuestionCreation() {
        expectFailure(InvalidQuestionException.class, "Blank question text is rejected",
                () -> new Question(7, "  ", "A1", "B1", "C1", "D1", "A"));
        expectFailure(InvalidQuestionException.class, "Blank option is rejected",
                () -> new Question(7, "Valid text?", "A1", " ", "C1", "D1", "A"));
        expectFailure(InvalidQuestionException.class, "Invalid answer label is rejected",
                () -> new Question(7, "Valid text?", "A1", "B1", "C1", "D1", "E"));
    }

    /** 7. A complete submission is graded into a passing result. */
    private static void testValidQuizSubmission() {
        check("Valid quiz submission produces a passing result", () -> {
            Result result = Result.evaluate(1, buildQuiz(),
                    new String[]{"C", "B"}, "2026-11-11");
            require(result.getScore() == 2, "Both answers should be correct");
            require(result.getPercentage() == 100.0, "Percentage should be 100");
            require(result.isPassed(), "Result should be a pass");
            require("A+".equals(result.getGrade()), "Grade should be A+");
        });
    }

    /** 8. A negative score is rejected by the Result invariant. */
    private static void testNegativeScore() {
        expectFailure(ValidationException.class, "Negative score is rejected",
                () -> new Result(0, 1, 1, -1, 5, 0.0, "2026-11-11"));
    }

    /** 9. A score above the question count is rejected. */
    private static void testScoreGreaterThanTotal() {
        expectFailure(ValidationException.class, "Score above total questions is rejected",
                () -> new Result(0, 1, 1, 6, 5, 0.0, "2026-11-11"));
    }

    /** 10. Incomplete or malformed submissions raise QuizSubmissionException. */
    private static void testInvalidQuizSubmission() {
        expectFailure(QuizSubmissionException.class, "Answer count mismatch is rejected",
                () -> Result.evaluate(1, buildQuiz(), new String[]{"A"}, "2026-11-11"));
        expectFailure(QuizSubmissionException.class, "Invalid answer label is rejected",
                () -> Result.evaluate(1, buildQuiz(), new String[]{"A", "Z"}, "2026-11-11"));
        expectFailure(QuizSubmissionException.class, "Unsaved quiz cannot be submitted",
                () -> Result.evaluate(1, new Quiz("Draft", "Programming", 5),
                        new String[0], "2026-11-11"));
    }

    private static Quiz buildQuiz() {
        Quiz quiz = new Quiz("Java Basics", "Programming", 10);
        quiz.setId(7);
        quiz.addQuestion(new Question(7, "Which keyword inherits a class?",
                "this", "super", "extends", "final", "C"));
        quiz.addQuestion(new Question(7, "Which collection keeps insertion order?",
                "HashSet", "ArrayList", "TreeSet", "HashMap", "B"));
        return quiz;
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
