package com.onlinequiz.model;

import com.onlinequiz.exception.QuizSubmissionException;
import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.interfaces.Identifiable;
import com.onlinequiz.util.Validation;

import java.util.Locale;

/**
 * Outcome of one quiz attempt. The percentage is recalculated whenever the score or
 * the question count changes.
 */
public class Result implements Identifiable {

    public static final double PASS_PERCENTAGE = 50.0;

    private int id;
    private int userId;
    private int quizId;
    private int score;
    private int totalQuestions;
    private double percentage;
    private String attemptDate;

    public Result(int id, int userId, int quizId, int score, int totalQuestions,
                  double percentage, String attemptDate) {
        setId(id);
        setUserId(userId);
        setQuizId(quizId);
        setTotalQuestions(totalQuestions);
        setScore(score);
        setPercentage(percentage);
        setAttemptDate(attemptDate);
    }

    /**
     * Constructor overloading: new attempts compute their own percentage.
     */
    public Result(int userId, int quizId, int score, int totalQuestions, String attemptDate) {
        setId(0);
        setUserId(userId);
        setQuizId(quizId);
        setTotalQuestions(totalQuestions);
        setScore(score);
        setAttemptDate(attemptDate);
    }

    @Override
    public final int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = Validation.requireNonNegative(id, "Result id");
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = Validation.requirePositive(userId, "User id");
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = Validation.requirePositive(quizId, "Quiz id");
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        if (score < 0 || score > totalQuestions) {
            throw new ValidationException(
                    "Score must be between 0 and " + totalQuestions);
        }
        this.score = score;
        this.percentage = calculatePercentage();
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        Validation.requirePositive(totalQuestions, "Total questions");
        if (score > totalQuestions) {
            throw new ValidationException(
                    "Total questions cannot be lower than the current score");
        }
        this.totalQuestions = totalQuestions;
        this.percentage = calculatePercentage();
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        if (percentage < 0.0 || percentage > 100.0) {
            throw new ValidationException("Percentage must be between 0 and 100");
        }
        this.percentage = round(percentage);
    }

    public String getAttemptDate() {
        return attemptDate;
    }

    public void setAttemptDate(String attemptDate) {
        this.attemptDate = Validation.requireText(attemptDate, "Attempt date");
    }

    /**
     * Recomputes and stores the percentage from the current score and total.
     */
    public double calculatePercentage() {
        this.percentage = round(score * 100.0 / totalQuestions);
        return percentage;
    }

    public boolean isPassed() {
        return percentage >= PASS_PERCENTAGE;
    }

    public String getGrade() {
        if (percentage >= 90.0) {
            return "A+";
        } else if (percentage >= 80.0) {
            return "A";
        } else if (percentage >= 70.0) {
            return "B";
        } else if (percentage >= 60.0) {
            return "C";
        } else if (percentage >= 50.0) {
            return "D";
        }
        return "F";
    }

    public String getScoreLabel() {
        return score + " / " + totalQuestions;
    }

    /**
     * Grades a set of selected answers against a saved quiz.
     *
     * Checked exception: an unanswered or malformed submission is an expected
     * user-facing problem (someone may click Submit too early), so the caller is
     * forced to catch {@link QuizSubmissionException} and show a helpful message.
     *
     * @param userId          account that submitted the quiz
     * @param quiz            saved quiz with at least one question
     * @param selectedAnswers one label per question, blank when skipped
     * @param attemptDate     human-readable timestamp supplied by the caller
     * @return a new result whose score and percentage are already calculated
     * @throws QuizSubmissionException when the quiz or the answers cannot be graded
     */
    public static Result evaluate(int userId, Quiz quiz, String[] selectedAnswers,
                                  String attemptDate) throws QuizSubmissionException {
        if (quiz == null) {
            throw new QuizSubmissionException("A quiz is required to evaluate an attempt");
        }
        if (quiz.getId() <= 0) {
            throw new QuizSubmissionException("Quiz must be saved before it can be submitted");
        }
        if (quiz.getQuestions().isEmpty()) {
            throw new QuizSubmissionException("Quiz '" + quiz.getTitle() + "' has no questions");
        }
        if (selectedAnswers == null) {
            throw new QuizSubmissionException("Answers must not be null");
        }

        int totalQuestions = quiz.getQuestions().size();
        if (selectedAnswers.length != totalQuestions) {
            throw new QuizSubmissionException("Expected " + totalQuestions
                    + " answers but received " + selectedAnswers.length);
        }

        boolean hasAnswer = false;
        for (int index = 0; index < selectedAnswers.length; index++) {
            String answer = selectedAnswers[index];
            if (answer == null || answer.trim().isEmpty()) {
                continue;
            }
            if (!isOptionLabel(answer)) {
                throw new QuizSubmissionException("Answer " + (index + 1)
                        + " ('" + answer.trim() + "') is not a valid option (A-D)");
            }
            hasAnswer = true;
        }
        if (!hasAnswer) {
            throw new QuizSubmissionException("At least one answer must be provided");
        }

        int score = 0;
        for (int index = 0; index < selectedAnswers.length; index++) {
            if (quiz.getQuestion(index).isCorrect(selectedAnswers[index])) {
                score++;
            }
        }
        return new Result(userId, quiz.getId(), score, totalQuestions, attemptDate);
    }

    private static boolean isOptionLabel(String answer) {
        String normalized = answer.trim().toUpperCase(Locale.ROOT);
        return "A".equals(normalized) || "B".equals(normalized)
                || "C".equals(normalized) || "D".equals(normalized);
    }

    public String displayResult() {
        return "RESULT #" + id
                + " | User " + userId
                + " | Quiz " + quizId
                + " | Score " + getScoreLabel()
                + " (" + percentage + "%)"
                + " | Grade " + getGrade()
                + " | " + (isPassed() ? "PASS" : "FAIL")
                + " | " + attemptDate;
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return "Result{id=" + id + ", userId=" + userId + ", quizId=" + quizId
                + ", score=" + getScoreLabel() + ", percentage=" + percentage + "}";
    }
}
