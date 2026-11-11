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

import java.util.ArrayList;

/**
 * Console demonstration of every exception-handling concept required by Phase 3.
 * Run with:
 * java -cp "target/classes;target/test-classes" com.onlinequiz.util.ExceptionDemo
 */
public final class ExceptionDemo {

    private ExceptionDemo() {
    }

    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println(" PHASE 3 - EXCEPTION HANDLING DEMONSTRATION");
        System.out.println("=====================================================");

        demonstrateBasicTryCatch();
        demonstrateMultipleCatch();
        demonstrateNestedTry();
        demonstrateFinally();
        demonstrateThrow();
        demonstrateThrows();
        demonstrateBuiltInExceptions();
        demonstrateCustomExceptions();
        demonstratePropagation();
        demonstrateCheckedVsUnchecked();

        System.out.println();
        System.out.println("DEMONSTRATION FINISHED - the program never crashed.");
    }

    /**
     * 1. Basic try-catch: risky code goes inside try, the matching catch handles it
     * and execution continues normally afterwards.
     */
    private static void demonstrateBasicTryCatch() {
        section("1. Basic try-catch");
        try {
            int divisor = 0;
            int result = 100 / divisor;
            System.out.println("Result: " + result);
        } catch (ArithmeticException error) {
            System.out.println("Caught " + error.getClass().getSimpleName()
                    + " -> " + error.getMessage());
        }
        System.out.println("Execution continues after the try-catch block.");
    }

    /**
     * 2. Multiple catch blocks: the runtime picks the first block whose type
     * matches the thrown exception. Order matters for related types.
     */
    private static void demonstrateMultipleCatch() {
        section("2. Multiple catch blocks");
        String[] inputs = {"12", "-", null};
        for (String input : inputs) {
            try {
                int index = Integer.parseInt(input);
                int[] values = new int[3];
                values[index] = 7;
                System.out.println("Stored 7 at index " + index);
            } catch (NumberFormatException error) {
                System.out.println("NumberFormatException for '" + input + "' -> "
                        + error.getMessage());
            } catch (ArrayIndexOutOfBoundsException error) {
                System.out.println("ArrayIndexOutOfBoundsException for '" + input + "' -> "
                        + error.getMessage());
            }
        }
    }

    /**
     * 3. Nested try: an inner try handles one failure while the surrounding try
     * guards the remaining work. Each level catches only what it understands.
     */
    private static void demonstrateNestedTry() {
        section("3. Nested try-catch");
        try {
            int[] scores = new int[2];
            try {
                scores[0] = 10 / 0;
            } catch (ArithmeticException error) {
                System.out.println("Inner catch handled: " + error.getMessage());
                scores[5] = 1;
            }
        } catch (ArrayIndexOutOfBoundsException error) {
            System.out.println("Outer catch handled the follow-up failure: " + error.getMessage());
        }
    }

    /**
     * 4. finally: always runs, whether the try succeeded, failed or was recovered.
     * It is the right place to release resources.
     */
    private static void demonstrateFinally() {
        section("4. finally");
        try {
            System.out.println("Opening quiz attempt ...");
            throw new QuizSubmissionException("Two answers are still blank");
        } catch (QuizSubmissionException error) {
            System.out.println("Recovered from: " + error.getMessage());
        } finally {
            System.out.println("finally: release the quiz lock / close resources here.");
        }
    }

    /**
     * 5. throw: code raises an exception deliberately when a rule is violated.
     */
    private static void demonstrateThrow() {
        section("5. throw");
        try {
            validateAge(-3);
        } catch (ValidationException error) {
            System.out.println("Explicitly thrown and caught: " + error.getMessage());
        }
    }

    /**
     * 6. throws: a method advertises a checked exception; the caller must handle
     * it. User.authenticate declares throws InvalidLoginException.
     */
    private static void demonstrateThrows() {
        section("6. throws (declaring checked exceptions)");
        Student student = new Student(1, "Asha Rao", "asha@example.com", "hash-123");
        try {
            student.authenticate("asha@example.com", "wrong-hash");
            System.out.println("Login accepted");
        } catch (InvalidLoginException error) {
            System.out.println("Caller handled declared exception: " + error.getMessage());
        }
    }

    /**
     * 7. Built-in exceptions thrown by the Java runtime itself.
     */
    private static void demonstrateBuiltInExceptions() {
        section("7. Built-in exceptions");
        showBuiltIn("ArithmeticException", () -> {
            int result = 5 / 0;
            System.out.println(result);
        });
        showBuiltIn("NullPointerException", () -> {
            String text = null;
            System.out.println(text.length());
        });
        showBuiltIn("ArrayIndexOutOfBoundsException", () -> {
            int[] values = new int[1];
            values[5] = 9;
        });
        showBuiltIn("NumberFormatException", () -> Integer.parseInt("ninety-nine"));
    }

    /**
     * 8. All five custom exceptions, each triggered through the real model code.
     */
    private static void demonstrateCustomExceptions() {
        section("8. Custom exceptions");

        // Unchecked: a negative id breaks a model invariant.
        try {
            new Student(-1, "Asha Rao", "asha@example.com", "hash-123");
        } catch (ValidationException error) {
            System.out.println("ValidationException (unchecked): " + error.getMessage());
        }

        // Unchecked and more specific: blank question text. The cause chain keeps
        // the original ValidationException for debugging.
        try {
            new Question(7, "   ", "A", "B", "C", "D", "A");
        } catch (InvalidQuestionException error) {
            System.out.println("InvalidQuestionException (unchecked): " + error.getMessage());
            System.out.println("  cause preserved: "
                    + (error.getCause() == null ? "none" : error.getCause().getClass().getSimpleName()));
            System.out.println("  safe user message: " + ExceptionHandler.getUserMessage(error));
        }

        // Checked: wrong credentials.
        Student student = new Student(1, "Asha Rao", "asha@example.com", "hash-123");
        try {
            student.authenticate("asha@example.com", "not-the-hash");
        } catch (InvalidLoginException error) {
            System.out.println("InvalidLoginException (checked): " + error.getMessage());
        }

        // Checked: the requested quiz is not in the collection.
        try {
            Quiz.findById(new ArrayList<>(), 99);
        } catch (QuizNotFoundException error) {
            System.out.println("QuizNotFoundException (checked): " + error.getMessage());
            System.out.println("  safe user message: " + ExceptionHandler.getUserMessage(error));
        }

        // Checked: answer count does not match the quiz.
        Quiz quiz = buildQuiz();
        try {
            Result.evaluate(1, quiz, new String[]{"A"}, "2026-11-11");
        } catch (QuizSubmissionException error) {
            System.out.println("QuizSubmissionException (checked): " + error.getMessage());
            System.out.println("  safe user message: " + ExceptionHandler.getUserMessage(error));
        }
    }

    /**
     * 9. Propagation: firstLayer() and secondLayer() do not catch anything, so the
     * exception travels up the call stack until main handles it.
     */
    private static void demonstratePropagation() {
        section("9. Exception propagation");
        try {
            firstLayer();
        } catch (QuizNotFoundException error) {
            System.out.println("main caught an exception raised two calls deeper: "
                    + error.getMessage());
        }
    }

    /**
     * 10. Checked versus unchecked. Checked exceptions must be caught or declared;
     * unchecked exceptions compile freely and usually signal programming errors.
     */
    private static void demonstrateCheckedVsUnchecked() {
        section("10. Checked versus unchecked");

        try {
            Quiz.findById(null, 1);
        } catch (QuizNotFoundException error) {
            System.out.println("checked (declared in the method signature): "
                    + error.getMessage());
        }

        try {
            new Result(1, 1, 1, -2, 5, 10.0, "2026-11-11");
        } catch (ValidationException error) {
            System.out.println("unchecked (no throws clause needed): " + error.getMessage());
        }

        System.out.println("Expected problem: " + ExceptionHandler.getUserMessage(
                new InvalidLoginException("Invalid email or password")));
        System.out.println("Unexpected problem: " + ExceptionHandler.getUserMessage(
                new IllegalStateException("connection string leaked")));
    }

    private static void validateAge(int age) {
        if (age < 0) {
            throw new ValidationException("Age must not be negative");
        }
        System.out.println("Age accepted: " + age);
    }

    private static void firstLayer() throws QuizNotFoundException {
        secondLayer();
    }

    private static void secondLayer() throws QuizNotFoundException {
        throw new QuizNotFoundException("Quiz 404 is missing");
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

    private static void showBuiltIn(String label, DangerousAction action) {
        try {
            action.run();
            System.out.println(label + " -> not thrown");
        } catch (Exception error) {
            System.out.println(label + " -> " + error.getClass().getSimpleName()
                    + ": " + error.getMessage());
        }
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("--- " + title + " ---");
    }

    @FunctionalInterface
    private interface DangerousAction {
        void run() throws Exception;
    }
}
