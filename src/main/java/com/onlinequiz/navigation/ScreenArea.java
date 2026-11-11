package com.onlinequiz.navigation;

/**
 * Every navigable area of the application. The audience attached to each area is
 * the single source of truth used for role-based access checks.
 */
public enum ScreenArea {

    WELCOME(Audience.PUBLIC),
    LOGIN(Audience.PUBLIC),
    REGISTER(Audience.PUBLIC),
    ADMIN_SETUP(Audience.PUBLIC),

    STUDENT_DASHBOARD(Audience.STUDENT),
    QUIZ_SELECTION(Audience.STUDENT),
    QUIZ_ATTEMPT(Audience.STUDENT),
    RESULT(Audience.STUDENT),
    HISTORY(Audience.STUDENT),

    ADMIN_DASHBOARD(Audience.ADMIN),
    QUIZ_MANAGEMENT(Audience.ADMIN),
    QUESTION_MANAGEMENT(Audience.ADMIN),
    STUDENT_MANAGEMENT(Audience.ADMIN),
    ANALYTICS(Audience.ADMIN);

    public enum Audience {
        PUBLIC,
        STUDENT,
        ADMIN
    }

    private final Audience audience;

    ScreenArea(Audience audience) {
        this.audience = audience;
    }

    public Audience getAudience() {
        return audience;
    }
}
