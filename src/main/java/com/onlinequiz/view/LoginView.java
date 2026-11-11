package com.onlinequiz.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Sign-in screen. Validation and authentication live in the controller; this
 * class only exposes the controls it needs.
 */
public final class LoginView extends StackPane {

    private final TextField emailField = UiFactory.textField("you@example.com");
    private final PasswordField passwordField = UiFactory.passwordField("Your password");
    private final Button loginButton = UiFactory.primaryButton("Sign in");
    private final Button registerButton = UiFactory.ghostButton("Create a student account");
    private final Button backButton = UiFactory.ghostButton("Back to welcome");
    private final Label message = UiFactory.messageLabel();

    public LoginView() {
        getStyleClass().add("auth-background");
        setPadding(new Insets(30));

        Label mark = new Label("Q");
        mark.getStyleClass().add("brand-mark");
        Label brandName = new Label("QUIZ STUDIO");
        brandName.getStyleClass().add("brand-name");
        Label brandTag = new Label("Secure sign-in");
        brandTag.getStyleClass().add("brand-tag");
        HBox brand = new HBox(11, mark, new VBox(1, brandName, brandTag));
        brand.setAlignment(Pos.CENTER_LEFT);

        Label badge = new Label("SECURE ACCESS");
        badge.getStyleClass().add("auth-badge");

        Label title = new Label("Sign in");
        title.getStyleClass().add("auth-title");
        Label subtitle = new Label("Students and administrators use the same secure sign-in."
                + " You will be routed to the correct dashboard.");
        subtitle.getStyleClass().add("auth-subtitle");
        subtitle.setWrapText(true);

        loginButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setMaxWidth(Double.MAX_VALUE);

        Label footnote = new Label("Credentials are verified against salted PBKDF2 hashes.");
        footnote.getStyleClass().add("auth-footnote");
        footnote.setWrapText(true);

        VBox card = new VBox(14,
                brand,
                badge,
                new VBox(5, title, subtitle),
                UiFactory.field("Email address", emailField),
                UiFactory.field("Password", passwordField),
                message,
                loginButton,
                UiFactory.separator(),
                registerButton,
                backButton,
                footnote);
        card.getStyleClass().add("auth-card");
        card.setPrefWidth(410);
        card.setMaxWidth(430);
        card.setMinWidth(320);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);
    }

    public TextField getEmailField() {
        return emailField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public Button getLoginButton() {
        return loginButton;
    }

    public Button getRegisterButton() {
        return registerButton;
    }

    public Button getBackButton() {
        return backButton;
    }

    public void setEmail(String email) {
        emailField.setText(email == null ? "" : email);
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
