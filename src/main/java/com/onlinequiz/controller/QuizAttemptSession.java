package com.onlinequiz.controller;

import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.model.Question;
import com.onlinequiz.model.Quiz;

import java.util.Arrays;
import java.util.Locale;

/**
 * Pure, JavaFX-free state for one quiz attempt. Keeping answer selection and
 * navigation here means the Previous/Next behaviour can be unit tested without
 * opening a window or touching the database.
 */
public final class QuizAttemptSession {

    private final Quiz quiz;
    private final String[] selectedAnswers;
    private int currentIndex;

    public QuizAttemptSession(Quiz quiz) {
        if (quiz == null) {
            throw new ValidationException("Quiz is required");
        }
        if (quiz.getId() <= 0) {
            throw new ValidationException("Quiz must be saved before it can be attempted");
        }
        if (quiz.getQuestions().isEmpty()) {
            throw new ValidationException("Quiz has no questions");
        }
        this.quiz = quiz;
        this.selectedAnswers = new String[quiz.getQuestions().size()];
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getCurrentNumber() {
        return currentIndex + 1;
    }

    public int getTotalQuestions() {
        return selectedAnswers.length;
    }

    public Question getCurrentQuestion() {
        return quiz.getQuestion(currentIndex);
    }

    public String getSelectedAnswer(int index) {
        return selectedAnswers[index];
    }

    public String getCurrentAnswer() {
        return selectedAnswers[currentIndex];
    }

    public void selectAnswer(String label) {
        selectAnswer(currentIndex, label);
    }

    public void selectAnswer(int index, String label) {
        if (index < 0 || index >= selectedAnswers.length) {
            throw new ValidationException("Question index is outside the quiz");
        }
        selectedAnswers[index] = normalizeLabel(label);
    }

    public boolean canGoPrevious() {
        return currentIndex > 0;
    }

    public boolean canGoNext() {
        return currentIndex < selectedAnswers.length - 1;
    }

    public void previous() {
        if (canGoPrevious()) {
            currentIndex--;
        }
    }

    public void next() {
        if (canGoNext()) {
            currentIndex++;
        }
    }

    public int getAnsweredCount() {
        int answered = 0;
        for (String answer : selectedAnswers) {
            if (answer != null) {
                answered++;
            }
        }
        return answered;
    }

    public boolean isComplete() {
        return getAnsweredCount() == selectedAnswers.length;
    }

    /**
     * Returns a defensive copy: grading must not be able to change the attempt
     * state through the returned array.
     */
    public String[] copyAnswers() {
        return Arrays.copyOf(selectedAnswers, selectedAnswers.length);
    }

    private static String normalizeLabel(String label) {
        if (label == null) {
            throw new ValidationException("Choose an option");
        }
        String normalized = label.trim().toUpperCase(Locale.ROOT);
        if (!"A".equals(normalized) && !"B".equals(normalized)
                && !"C".equals(normalized) && !"D".equals(normalized)) {
            throw new ValidationException("Answer must be A, B, C or D");
        }
        return normalized;
    }
}
