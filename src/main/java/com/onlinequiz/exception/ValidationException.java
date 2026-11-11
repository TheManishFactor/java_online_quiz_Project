package com.onlinequiz.exception;

/**
 * Unchecked validation failure used by the shared input guards.
 *
 * It extends IllegalArgumentException so existing defensive checks and callers
 * that catch IllegalArgumentException keep working, while the application can
 * still distinguish its own validation failures.
 *
 * Checked versus unchecked: validation errors are usually caused by bad caller
 * input and should fail fast. Keeping this unchecked avoids forcing a throws
 * clause onto every model constructor and setter.
 */
public class ValidationException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
