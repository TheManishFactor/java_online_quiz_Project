package com.onlinequiz.exception;

/**
 * Thrown when question content fails validation: blank text, blank options or an
 * answer label outside A-D.
 *
 * It extends ValidationException, which gives a clear exception hierarchy while
 * remaining unchecked (invalid question data is a fail-fast programming error).
 */
public class InvalidQuestionException extends ValidationException {

    private static final long serialVersionUID = 1L;

    public InvalidQuestionException(String message) {
        super(message);
    }

    public InvalidQuestionException(String message, Throwable cause) {
        super(message, cause);
    }
}
