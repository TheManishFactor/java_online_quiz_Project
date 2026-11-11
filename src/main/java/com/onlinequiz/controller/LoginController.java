package com.onlinequiz.controller;

import com.onlinequiz.exception.InvalidLoginException;
import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.model.User;
import com.onlinequiz.navigation.NavigationManager;
import com.onlinequiz.service.UserService;
import com.onlinequiz.util.ExceptionHandler;
import com.onlinequiz.view.LoginView;
import javafx.scene.input.KeyCode;

/**
 * Authenticates through UserService and routes the result to the correct
 * dashboard. The same generic failure message is used for unknown email and wrong
 * password so the screen does not reveal which accounts exist.
 */
public final class LoginController {

    private final NavigationManager navigation;
    private final UserService userService;
    private final LoginView view;

    public LoginController(NavigationManager navigation, UserService userService,
                           LoginView view) {
        this.navigation = navigation;
        this.userService = userService;
        this.view = view;

        view.getLoginButton().setOnAction(event -> login());
        view.getRegisterButton().setOnAction(event -> navigation.showRegister());
        view.getBackButton().setOnAction(event -> navigation.showWelcome());
        view.getPasswordField().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                login();
            }
        });
        view.getEmailField().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                login();
            }
        });
    }

    private void login() {
        view.clearMessage();
        String email = view.getEmailField().getText();
        String password = view.getPasswordField().getText();

        try {
            if (email == null || email.trim().isEmpty()) {
                throw new ValidationException("Email is required");
            }
            if (password == null || password.isEmpty()) {
                throw new ValidationException("Password is required");
            }
            User user = userService.loginUser(email, password);
            navigation.loginSucceeded(user);
        } catch (InvalidLoginException | ValidationException error) {
            view.setError(error.getMessage());
            view.getPasswordField().clear();
        } catch (RuntimeException error) {
            ExceptionHandler.handle(error, "Login");
            view.setError("Sign-in could not be completed. Please try again.");
        }
    }
}
