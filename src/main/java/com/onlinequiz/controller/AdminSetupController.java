package com.onlinequiz.controller;

import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.navigation.NavigationManager;
import com.onlinequiz.service.UserService;
import com.onlinequiz.util.ExceptionHandler;
import com.onlinequiz.util.RegistrationValidator;
import com.onlinequiz.view.AdminSetupView;
import javafx.scene.input.KeyCode;

/**
 * Secure first-run administrator creation. No default credentials exist: the
 * installer chooses them and the service refuses the call once an administrator
 * is already present.
 */
public final class AdminSetupController {

    private final NavigationManager navigation;
    private final UserService userService;
    private final AdminSetupView view;

    public AdminSetupController(NavigationManager navigation, UserService userService,
                                AdminSetupView view) {
        this.navigation = navigation;
        this.userService = userService;
        this.view = view;

        view.getCreateButton().setOnAction(event -> createAdmin());
        view.getBackButton().setOnAction(event -> navigation.showWelcome());
        view.getConfirmField().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                createAdmin();
            }
        });
    }

    private void createAdmin() {
        view.clearMessage();
        try {
            String name = RegistrationValidator.requireName(view.getNameField().getText());
            String email = RegistrationValidator.requireEmail(view.getEmailField().getText());
            String password = view.getPasswordField().getText();
            RegistrationValidator.requireMatchingPasswords(password,
                    view.getConfirmField().getText());

            userService.createFirstAdmin(name, email, password);
            navigation.showLogin(email, "Administrator account created. Sign in to continue.");
        } catch (ValidationException error) {
            view.setError(error.getMessage());
        } catch (RuntimeException error) {
            ExceptionHandler.handle(error, "Administrator setup");
            view.setError("Administrator setup could not be completed. Please try again.");
        }
    }
}
