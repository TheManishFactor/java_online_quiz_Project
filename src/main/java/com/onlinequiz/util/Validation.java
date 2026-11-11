package com.onlinequiz.util;

import com.onlinequiz.exception.ValidationException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Stateless guards used by the domain model to protect its invariants.
 *
 * Every guard throws the unchecked {@link ValidationException}, so callers are not
 * forced to declare or catch it, while still receiving a domain-specific error type.
 */
public final class Validation {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Validation() {
    }

    public static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    public static String requireEmail(String value) {
        String email = requireText(value, "Email").toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Email format is invalid");
        }
        return email;
    }

    public static int requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new ValidationException(fieldName + " must not be negative");
        }
        return value;
    }

    public static int requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be greater than zero");
        }
        return value;
    }
}
