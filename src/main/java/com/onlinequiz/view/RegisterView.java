package com.onlinequiz.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Student registration. The role is intentionally fixed to STUDENT: visitors can
 * never register themselves as administrators.
 */
public final class RegisterView extends StackPane {

    private final TextField nameField = UiFactory.textField("Full name");
    private final TextField emailField = UiFactory.textField("you@example.com");
    private final PasswordField passwordField = UiFactory.passwordField("At least 8 characters");
    private final PasswordField confirmField = UiFactory.passwordField("Repeat the password");
    private final Button registerButton = UiFactory.primaryButton("Create student account");
    private final Button loginButton = UiFactory.ghostButton("I already have an account");
    private final Button backButton = UiFactory.ghostButton("Back to welcome");
    private final Label message = UiFactory.messageLabel();

    public RegisterView() {
        getStyleClass().add("auth-background");

        Label mark = new Label("Q");
        mark.getStyleClass().add("brand-mark");
        Label brandName = new Label("QUIZ STUDIO");
        brandName.getStyleClass().add("brand-name");
        Label brandTag = new Label("Student registration");
        brandTag.getStyleClass().add("brand-tag");
        HBox brand = new HBox(11, mark, new VBox(1, brandName, brandTag));
        brand.setAlignment(Pos.CENTER_LEFT);

        Label badge = new Label("NEW STUDENT");
        badge.getStyleClass().add("auth-badge");

        Label title = new Label("Create your account");
        title.getStyleClass().add("auth-title");
        Label subtitle = new Label("Register once, then sign in to attempt quizzes and keep"
                + " a permanent result history.");
        subtitle.getStyleClass().add("auth-subtitle");
        subtitle.setWrapText(true);

        Label roleCaption = new Label("Account type");
        roleCaption.getStyleClass().add("field-label");
        HBox roleRow = new HBox(10, roleCaption, UiFactory.chip("Student"));
        roleRow.setAlignment(Pos.CENTER_LEFT);

        Label roleHint = new Label("Administrator accounts are created only through the secure"
                + " first-run setup.");
        roleHint.getStyleClass().add("field-hint");
        roleHint.setWrapText(true);

        registerButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setMaxWidth(Double.MAX_VALUE);

        VBox card = new VBox(12,
                brand,
                badge,
                new VBox(5, title, subtitle),
                UiFactory.field("Full name", nameField),
                UiFactory.field("Email address", emailField),
                new VBox(5, roleRow, roleHint),
                UiFactory.field("Password", passwordField),
                UiFactory.field("Confirm password", confirmField),
                message,
                registerButton,
                UiFactory.separator(),
                loginButton,
                backButton);
        card.getStyleClass().add("auth-card");
        card.setPrefWidth(440);
        card.setMaxWidth(470);
        card.setMinWidth(320);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        StackPane cardHost = new StackPane(card);
        cardHost.setPadding(new Insets(24));
        cardHost.setMinWidth(0);
        ScrollPane scroll = UiFactory.scroll(cardHost);
        scroll.setFitToHeight(true);
        getChildren().add(scroll);
    }

    public TextField getNameField() {
        return nameField;
    }

    public TextField getEmailField() {
        return emailField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public PasswordField getConfirmField() {
        return confirmField;
    }

    public Button getRegisterButton() {
        return registerButton;
    }

    public Button getLoginButton() {
        return loginButton;
    }

    public Button getBackButton() {
        return backButton;
    }

    public void setError(String text) {
        UiFactory.error(message, text);
    }

    public void setSuccess(String text) {
        UiFactory.success(message, text);
    }

    public void clearMessage() {
        UiFactory.clear(message);
    }
}
