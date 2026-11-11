package com.onlinequiz.exception;

/**
 * Thrown when supplied credentials do not match an account.
 *
 * Checked exception: a failed login is an expected, recoverable outcome. The
 * caller must consciously decide how to report it to the user.
 */
public class InvalidLoginException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidLoginException(String message) {
        super(message);
    }

    public InvalidLoginException(String message, Throwable cause) {
        super(message, cause);
    }
}
