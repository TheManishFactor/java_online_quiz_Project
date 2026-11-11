package com.onlinequiz.model;

import com.onlinequiz.interfaces.Identifiable;

/**
 * Abstract root of the user hierarchy. This class is stateless on purpose: it
 * declares the identity contract and supplies shared behaviour, while {@link User}
 * owns the account fields.
 *
 * Inheritance chain: Person -> User -> Student/Admin (multilevel inheritance).
 */
public abstract class Person implements Identifiable {

    public abstract String getName();

    public abstract String getEmail();

    public abstract String getRole();

    /**
     * Shared behaviour: derives up to two initials from the person's name.
     */
    public String getInitials() {
        String[] parts = getName().trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(Character.toUpperCase(part.charAt(0)));
            }
            if (initials.length() == 2) {
                break;
            }
        }
        return initials.length() == 0 ? "?" : initials.toString();
    }
}
