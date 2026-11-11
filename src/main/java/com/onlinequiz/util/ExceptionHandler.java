package com.onlinequiz.util;

import com.onlinequiz.exception.InvalidLoginException;
import com.onlinequiz.exception.QuizNotFoundException;
import com.onlinequiz.exception.QuizSubmissionException;
import com.onlinequiz.exception.ValidationException;

/**
 * Central helper for exception handling. It separates the message shown to a user
 * from the technical detail written to the console/log, so internal details are
 * never exposed in the UI.
 */
public final class ExceptionHandler {

    private ExceptionHandler() {
    }

    /**
     * Produces a safe, understandable message. Unexpected exceptions deliberately
     * hide their internal message from the user.
     */
    public static String getUserMessage(Throwable throwable) {
        if (throwable == null) {
            return "An unexpected error occurred. Please try again.";
        }
        if (throwable instanceof InvalidLoginException) {
            return "Login failed: " + safeMessage(throwable);
        }
        if (throwable instanceof QuizNotFoundException) {
            return "Quiz not available: " + safeMessage(throwable);
        }
        if (throwable instanceof QuizSubmissionException) {
            return "Submission failed: " + safeMessage(throwable);
        }
        if (throwable instanceof ValidationException) {
            return "Invalid input: " + safeMessage(throwable);
        }
        return "An unexpected error occurred. Please try again.";
    }

    /**
     * Reports a handled exception. Expected application exceptions are logged with
     * their message and cause; unexpected exceptions keep a full stack trace in the
     * technical log.
     */
    public static void handle(Throwable throwable, String context) {
        String scope = (context == null || context.trim().isEmpty())
                ? "Application" : context.trim();
        System.err.println("[" + scope + "] " + getUserMessage(throwable));
        log(throwable);
    }

    public static void log(Throwable throwable) {
        if (throwable == null) {
            System.err.println("No exception details were supplied.");
            return;
        }
        System.err.println("Cause: " + throwable.getClass().getSimpleName()
                + ": " + safeMessage(throwable));

        if (isExpectedApplicationException(throwable)) {
            Throwable cause = throwable.getCause();
            if (cause != null) {
                System.err.println("Caused by: " + cause.getClass().getSimpleName()
                        + ": " + safeMessage(cause));
            }
            return;
        }
        throwable.printStackTrace(System.err);
    }

    private static boolean isExpectedApplicationException(Throwable throwable) {
        return throwable instanceof InvalidLoginException
                || throwable instanceof QuizNotFoundException
                || throwable instanceof QuizSubmissionException
                || throwable instanceof ValidationException;
    }

    private static String safeMessage(Throwable throwable) {
        String message = throwable.getMessage();
        return (message == null || message.trim().isEmpty())
                ? "no details available" : message;
    }
}
