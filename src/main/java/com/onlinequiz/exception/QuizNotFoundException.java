package com.onlinequiz.exception;

/**
 * Thrown when a quiz lookup cannot find the requested quiz.
 *
 * Checked exception: the caller must handle the missing record, for example by
 * refreshing the quiz list or showing a not-found message.
 */
public class QuizNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public QuizNotFoundException(String message) {
        super(message);
    }

    public QuizNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
