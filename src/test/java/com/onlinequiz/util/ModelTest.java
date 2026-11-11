package com.onlinequiz.util;

import com.onlinequiz.model.Admin;
import com.onlinequiz.model.Person;
import com.onlinequiz.model.Question;
import com.onlinequiz.model.Quiz;
import com.onlinequiz.model.Result;
import com.onlinequiz.model.Student;
import com.onlinequiz.model.User;

/**
 * Console-based verification of the Phase 2 model layer. Run with:
 * java -cp "target/classes;target/test-classes" com.onlinequiz.util.ModelTest
 */
public final class ModelTest {

    private static int checks;

    private ModelTest() {
    }

    public static void main(String[] args) {
        try {
            demonstrateUsers();
            demonstrateQuizManagement();
            demonstrateValidation();
            demonstrateResult();
            System.out.println();
            System.out.println("ALL MODEL CHECKS PASSED (" + checks + " checks)");
        } catch (RuntimeException | AssertionError error) {
            System.err.println("MODEL CHECKS FAILED: " + error.getMessage());
            error.printStackTrace();
            System.exit(1);
        }
    }

    private static void demonstrateUsers() {
        section("1. Users, inheritance and runtime polymorphism");

        Student student = new Student(1, "Asha Rao", "Asha@Example.com",
                "hash-student", 2, 72.5);
        User studentRef = student;

        studentRef.displayBasicInfo();
        System.out.println();
        System.out.println(studentRef.displayDashboard());

        check("STUDENT".equals(studentRef.getRole()), "Student reports the STUDENT role");
        check(!studentRef.isAdmin(), "Student has no admin privileges");
        check("asha@example.com".equals(studentRef.getEmail()), "Email is normalized to lower case");
        check("AR".equals(studentRef.getInitials()), "Initials come from the shared Person behaviour");
        check(studentRef instanceof Person, "Student is a Person through multilevel inheritance");

        student.recordAttempt(88.0);
        check(student.getTotalAttempts() == 3, "recordAttempt increments totalAttempts");
        check(Math.abs(student.getAverageScore() - 77.67) < 0.001,
                "recordAttempt recalculates the running average");

        Student freshStudent = new Student("Ravi Kumar", "ravi@example.com", "hash-ravi");
        check(freshStudent.getTotalAttempts() == 0, "New student starts with zero attempts");

        Admin admin = new Admin("Admin User", "admin@quiz.com", "hash-admin", Admin.LEVEL_SUPER);
        User adminRef = admin;
        System.out.println();
        System.out.println(adminRef.displayDashboard());

        check("ADMIN".equals(adminRef.getRole()), "Admin reports the ADMIN role");
        check(adminRef.isAdmin() && adminRef.canManageQuizzes(), "Admin can manage quizzes");
        check(admin.canManageUsers(), "SUPER admin can manage users");
    }

    private static void demonstrateQuizManagement() {
        section("2. Quiz and question management");

        Quiz quiz = new Quiz(10, "Java Fundamentals", "Java", 15, 0);

        Question q1 = new Question(101, 10, "Which keyword is used to inherit a class?",
                "implements", "extends", "imports", "package", "B");
        Question q2 = new Question(102, 10, "Which primitive type stores true or false?",
                "int", "double", "boolean", "char", "C");
        Question unsaved = new Question(10, "Which collection keeps insertion order?",
                "ArrayList", "HashSet", "TreeSet", "Hashtable", "A");

        quiz.addQuestion(q1);
        Admin admin = new Admin("Admin User", "admin@quiz.com", "hash-admin");
        admin.addQuestion(quiz, q2);

        check(unsaved.getId() == 0, "Constructor overload supports an unsaved question");
        check(quiz.getQuestions().size() == 2, "Quiz stores questions in an ArrayList");
        check(quiz.getTotalQuestions() == 2, "Adding questions keeps totalQuestions in sync");
        check(quiz.getQuestion(1) == q2, "Questions are retrievable by index");
        check(q1.getOptions().length == 4, "Every question exposes an array of four options");
        check(q1.isCorrect("b"), "Answer checking is case-insensitive");
        check(!q1.isCorrect("A"), "A wrong answer is rejected");
        check("15 minutes".equals(quiz.getDurationLabel()), "Duration label is formatted");

        check(quiz.removeQuestion(102), "removeQuestion removes by id");
        check(quiz.getQuestions().size() == 1, "Removed question disappears from the list");
        check(quiz.getTotalQuestions() == 1, "Removing a question updates the cached count");
        check(!quiz.removeQuestion(999), "Removing an unknown id returns false");

        Quiz created = admin.createQuiz("Java Collections", "Java", 10);
        check("Java Collections".equals(created.getTitle()), "Admin creates a new quiz object");
    }

    private static void demonstrateValidation() {
        section("3. Input validation");

        expectIllegalArgument(
                () -> new Question(10, "Bad answer", "A", "B", "C", "D", "E"),
                "Question rejects answers outside A-D");
        expectIllegalArgument(
                () -> new Question(10, "Bad option", "A", "  ", "C", "D", "B"),
                "Question rejects blank options");
        expectIllegalArgument(
                () -> new Quiz("Bad quiz", "Java", 0),
                "Quiz rejects a non-positive duration");
        expectIllegalArgument(
                () -> new Result(1, 10, 11, 10, "2026-11-11"),
                "Result rejects a score greater than the total");
        expectIllegalArgument(
                () -> new Student("Bad Email", "invalid", "hash"),
                "User rejects malformed email addresses");
    }

    private static void demonstrateResult() {
        section("4. Result calculation and display");

        Result result = new Result(1, 10, 8, 10, "2026-11-11 10:30");
        System.out.println(result.displayResult());

        check(Math.abs(result.getPercentage() - 80.0) < 0.001,
                "Constructor computes 8/10 as 80 percent");
        check("A".equals(result.getGrade()), "80 percent maps to grade A");
        check(result.isPassed(), "80 percent is a pass");
        check("8 / 10".equals(result.getScoreLabel()), "Score label is formatted");

        result.setScore(7);
        check(Math.abs(result.getPercentage() - 70.0) < 0.001,
                "setScore recalculates the percentage");
        check("B".equals(result.getGrade()), "Grade changes when the score changes");
        check(Math.abs(result.calculatePercentage() - 70.0) < 0.001,
                "calculatePercentage returns the current percentage");

        result.setAttemptDate("2026-11-12 09:00");
        check("2026-11-12 09:00".equals(result.getAttemptDate()),
                "Attempt date setter stores the supplied value");
    }

    private static void expectIllegalArgument(Runnable action, String description) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            pass(description);
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException: " + description);
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
        pass(description);
    }

    private static void pass(String description) {
        checks++;
        System.out.println("[PASS] " + description);
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("--- " + title + " ---");
    }
}
