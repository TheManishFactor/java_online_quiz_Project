package com.onlinequiz.navigation;

import com.onlinequiz.model.User;

/**
 * Central role-based access rule. Both the navigation manager and the automated
 * tests use this class, so the screens cannot drift away from the tested policy.
 */
public final class AccessPolicy {

    private AccessPolicy() {
    }

    public static boolean canAccess(User user, ScreenArea area) {
        if (area == null) {
            return false;
        }
        switch (area.getAudience()) {
            case PUBLIC:
                return true;
            case STUDENT:
                return isStudent(user);
            case ADMIN:
                return isAdmin(user);
            default:
                return false;
        }
    }

    public static boolean isStudent(User user) {
        return user != null && !user.isAdmin();
    }

    public static boolean isAdmin(User user) {
        return user != null && user.isAdmin();
    }
}
