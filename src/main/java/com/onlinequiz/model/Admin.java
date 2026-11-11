package com.onlinequiz.model;

import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.util.Validation;

import java.util.Locale;

/**
 * Administrator account with quiz-administration behaviour.
 *
 * Single inheritance: Admin extends User and overrides displayDashboard().
 */
public final class Admin extends User {

    public static final String ROLE = User.ROLE_ADMIN;
    public static final String LEVEL_STANDARD = "STANDARD";
    public static final String LEVEL_SUPER = "SUPER";

    private String adminLevel;

    public Admin(int id, String name, String email, String passwordHash, String adminLevel) {
        super(id, name, email, passwordHash, ROLE);
        this.adminLevel = normalizeLevel(adminLevel);
    }

    /**
     * Constructor overloading: administrators default to STANDARD level.
     */
    public Admin(int id, String name, String email, String passwordHash) {
        this(id, name, email, passwordHash, LEVEL_STANDARD);
    }

    public Admin(String name, String email, String passwordHash) {
        this(0, name, email, passwordHash);
    }

    public Admin(String name, String email, String passwordHash, String adminLevel) {
        this(0, name, email, passwordHash, adminLevel);
    }

    public String getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(String adminLevel) {
        this.adminLevel = normalizeLevel(adminLevel);
    }

    public boolean canManageUsers() {
        return LEVEL_SUPER.equals(adminLevel);
    }

    /**
     * Administration use cases delegate to the Quiz aggregate.
     */
    public Quiz createQuiz(String title, String category, int duration) {
        return new Quiz(title, category, duration);
    }

    public void addQuestion(Quiz quiz, Question question) {
        if (quiz == null) {
            throw new ValidationException("Quiz is required");
        }
        quiz.addQuestion(question);
    }

    public boolean removeQuestion(Quiz quiz, int questionId) {
        if (quiz == null) {
            throw new ValidationException("Quiz is required");
        }
        return quiz.removeQuestion(questionId);
    }

    @Override
    public boolean isAdmin() {
        return true;
    }

    @Override
    public String displayDashboard() {
        return "ADMIN DASHBOARD" + System.lineSeparator()
                + "Administrator : " + getName() + System.lineSeparator()
                + "Level         : " + adminLevel + System.lineSeparator()
                + "Quiz access   : full management";
    }

    private static String normalizeLevel(String level) {
        String normalized = Validation.requireText(level, "Admin level").toUpperCase(Locale.ROOT);
        if (!LEVEL_STANDARD.equals(normalized) && !LEVEL_SUPER.equals(normalized)) {
            throw new ValidationException("Admin level must be STANDARD or SUPER");
        }
        return normalized;
    }
}
