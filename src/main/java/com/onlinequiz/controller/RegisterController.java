package com.onlinequiz.controller;

import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.model.Student;
import com.onlinequiz.navigation.NavigationManager;
import com.onlinequiz.service.UserService;
import com.onlinequiz.util.ExceptionHandler;
import com.onlinequiz.util.RegistrationValidator;
import com.onlinequiz.view.RegisterView;
import javafx.scene.input.KeyCode;

/**
 * Student self-registration. The service hashes the password and rejects duplicate
 * emails; the controller adds the form-specific password confirmation check and a
 * fixed STUDENT role so nobody can self-register as an administrator.
 */
public final class RegisterController {

    private final NavigationManager navigation;
    private final UserService userService;
    private final RegisterView view;

    public RegisterController(NavigationManager navigation, UserService userService,
                              RegisterView view) {
        this.navigation = navigation;
        this.userService = userService;
        this.view = view;

        view.getRegisterButton().setOnAction(event -> register());
        view.getLoginButton().setOnAction(event -> navigation.showLogin());
        view.getBackButton().setOnAction(event -> navigation.showWelcome());
        view.getConfirmField().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                register();
            }
        });
    }

    private void register() {
        view.clearMessage();
        try {
            String name = RegistrationValidator.requireName(view.getNameField().getText());
            String email = RegistrationValidator.requireEmail(view.getEmailField().getText());
            String password = view.getPasswordField().getText();
            RegistrationValidator.requireMatchingPasswords(password,
                    view.getConfirmField().getText());

            Student student = new Student(name, email, password);
            userService.registerUser(student);
            navigation.showLogin(email, "Student account created. Sign in to continue.");
        } catch (ValidationException error) {
            view.setError(error.getMessage());
        } catch (RuntimeException error) {
            ExceptionHandler.handle(error, "Registration");
            view.setError("Registration could not be completed. Please try again.");
        }
    }
}
