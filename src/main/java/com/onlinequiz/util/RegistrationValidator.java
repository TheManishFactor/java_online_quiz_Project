package com.onlinequiz.util;

import com.onlinequiz.exception.ValidationException;

/**
 * Shared validation for the registration and first-run administrator forms.
 * Keeping these rules outside the JavaFX controllers makes them testable without
 * starting a GUI and guarantees both forms apply the same password policy.
 */
public final class RegistrationValidator {

    public static final int MIN_PASSWORD_LENGTH = 8;

    private RegistrationValidator() {
    }

    public static String requireName(String name) {
        return Validation.requireText(name, "Full name");
    }

    public static String requireEmail(String email) {
        return Validation.requireEmail(email);
    }

    /**
     * Validates a plain-text password before it is hashed.
     */
    public static String requirePassword(String password) {
        String value = Validation.requireText(password, "Password");
        if (value.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        return value;
    }

    /**
     * Confirms that the two password fields match.
     */
    public static void requireMatchingPasswords(String password, String confirmation) {
        requirePassword(password);
        if (confirmation == null || !password.equals(confirmation)) {
            throw new ValidationException("Passwords do not match");
        }
    }
}
