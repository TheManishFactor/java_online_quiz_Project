package com.onlinequiz.model;

import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.util.Validation;

/**
 * Student account with in-memory attempt statistics.
 *
 * Single inheritance: Student extends User. The super() call initializes the
 * inherited account state before the student-specific fields are assigned.
 */
public final class Student extends User {

    public static final String ROLE = User.ROLE_STUDENT;

    private int totalAttempts;
    private double averageScore;

    public Student(int id, String name, String email, String passwordHash,
                   int totalAttempts, double averageScore) {
        super(id, name, email, passwordHash, ROLE);
        setTotalAttempts(totalAttempts);
        setAverageScore(averageScore);
    }

    /**
     * Constructor overloading: a brand new student starts with empty statistics.
     */
    public Student(int id, String name, String email, String passwordHash) {
        this(id, name, email, passwordHash, 0, 0.0);
    }

    public Student(String name, String email, String passwordHash) {
        this(0, name, email, passwordHash);
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = Validation.requireNonNegative(totalAttempts, "Total attempts");
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        if (averageScore < 0.0 || averageScore > 100.0) {
            throw new ValidationException("Average score must be between 0 and 100");
        }
        this.averageScore = Math.round(averageScore * 100.0) / 100.0;
    }

    /**
     * Updates the running average without keeping the full result history in memory.
     */
    public void recordAttempt(double percentage) {
        if (percentage < 0.0 || percentage > 100.0) {
            throw new ValidationException("Percentage must be between 0 and 100");
        }
        double combined = averageScore * totalAttempts + percentage;
        totalAttempts++;
        averageScore = Math.round((combined / totalAttempts) * 100.0) / 100.0;
    }

    /**
     * Method overloading: accepts raw score data and delegates to the percentage
     * version above.
     */
    public void recordAttempt(int score, int totalQuestions) {
        if (totalQuestions <= 0 || score < 0 || score > totalQuestions) {
            throw new ValidationException("Score must be between 0 and " + totalQuestions);
        }
        double percentage = Math.round((score * 100.0 / totalQuestions) * 100.0) / 100.0;
        recordAttempt(percentage);
    }

    @Override
    public boolean isAdmin() {
        return false;
    }

    @Override
    public String displayDashboard() {
        return "STUDENT DASHBOARD" + System.lineSeparator()
                + "Student  : " + getName() + System.lineSeparator()
                + "Attempts : " + totalAttempts + System.lineSeparator()
                + "Average  : " + averageScore + "%";
    }
}
