package com.onlinequiz.controller;

import com.onlinequiz.navigation.NavigationManager;
import com.onlinequiz.service.UserService;
import com.onlinequiz.util.ExceptionHandler;
import com.onlinequiz.view.WelcomeView;

/**
 * Wires the landing screen. The first-run administrator link is shown only when
 * the user table contains no administrator yet.
 */
public final class WelcomeController {

    public WelcomeController(NavigationManager navigation, UserService userService,
                             WelcomeView view) {
        view.getLoginButton().setOnAction(event -> navigation.showLogin());
        view.getRegisterButton().setOnAction(event -> navigation.showRegister());
        view.getSetupButton().setOnAction(event -> navigation.showAdminSetup());

        boolean hasAdmin;
        try {
            hasAdmin = userService.hasAdmin();
        } catch (RuntimeException error) {
            ExceptionHandler.handle(error, "Administrator check");
            hasAdmin = true;
        }
        view.showAdminSetupLink(!hasAdmin);
    }
}
