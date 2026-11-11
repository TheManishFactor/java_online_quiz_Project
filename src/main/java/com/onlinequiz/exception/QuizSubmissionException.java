package com.onlinequiz.exception;

/**
 * Thrown when a quiz submission cannot be processed: no answers were supplied, the
 * answer count is wrong, the quiz is unsaved or an answer label is invalid.
 *
 * Checked exception: the caller must handle the incomplete submission and ask the
 * student to correct it before a result can be calculated.
 */
public class QuizSubmissionException extends Exception {

    private static final long serialVersionUID = 1L;

    public QuizSubmissionException(String message) {
        super(message);
    }

    public QuizSubmissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
