package com.onlinequiz.controller;

import com.onlinequiz.exception.QuizNotFoundException;
import com.onlinequiz.exception.QuizSubmissionException;
import com.onlinequiz.model.Quiz;
import com.onlinequiz.model.Result;
import com.onlinequiz.service.ResultService;

/**
 * Single-entry submission guard for one attempt.
 *
 * Both the Submit button and the timer expiry call this object. Synchronization
 * plus the submitted flag make a duplicate save impossible even when both paths
 * fire at nearly the same moment.
 */
public final class AttemptSubmission {

    private boolean submitting;
    private boolean submitted;

    /**
     * Grades and persists the attempt exactly once.
     *
     * @param allowEmpty when true (timer expiry) an entirely blank attempt is saved
     *                   with score 0; a manual submission still requires an answer
     * @return the saved result with its database id
     */
    public synchronized Result submit(int userId, Quiz quiz, String[] answers,
                                      String attemptDate, boolean allowEmpty,
                                      ResultService resultService)
            throws QuizSubmissionException, QuizNotFoundException {
        if (submitted) {
            throw new QuizSubmissionException("This attempt has already been submitted");
        }
        if (submitting) {
            throw new QuizSubmissionException("This attempt is already being submitted");
        }
        if (quiz == null || quiz.getId() <= 0) {
            throw new QuizSubmissionException("Quiz must be saved before it can be submitted");
        }
        if (quiz.getQuestions().isEmpty()) {
            throw new QuizSubmissionException("Quiz has no questions");
        }

        submitting = true;
        try {
            Result result;
            if (allowEmpty && !hasAnyAnswer(answers)) {
                result = new Result(userId, quiz.getId(), 0,
                        quiz.getQuestions().size(), attemptDate);
            } else {
                result = Result.evaluate(userId, quiz, answers, attemptDate);
            }
            Result saved = resultService.saveResult(result);
            submitted = true;
            return saved;
        } finally {
            submitting = false;
        }
    }

    public synchronized boolean isSubmitted() {
        return submitted;
    }

    private boolean hasAnyAnswer(String[] answers) {
        if (answers == null) {
            return false;
        }
        for (String answer : answers) {
            if (answer != null && !answer.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
