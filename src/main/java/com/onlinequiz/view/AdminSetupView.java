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
 * One-time administrator setup. It is offered only while the database contains no
 * administrator, and the password is chosen here instead of being hardcoded.
 */
public final class AdminSetupView extends StackPane {

    private final TextField nameField = UiFactory.textField("Administrator full name");
    private final TextField emailField = UiFactory.textField("admin@yourdomain.com");
    private final PasswordField passwordField = UiFactory.passwordField("At least 8 characters");
    private final PasswordField confirmField = UiFactory.passwordField("Repeat the password");
    private final Button createButton = UiFactory.primaryButton("Create administrator");
    private final Button backButton = UiFactory.ghostButton("Back to welcome");
    private final Label message = UiFactory.messageLabel();

    public AdminSetupView() {
        getStyleClass().add("auth-background");

        Label mark = new Label("Q");
        mark.getStyleClass().add("brand-mark");
        Label brandName = new Label("QUIZ STUDIO");
        brandName.getStyleClass().add("brand-name");
        Label brandTag = new Label("First-run setup");
        brandTag.getStyleClass().add("brand-tag");
        HBox brand = new HBox(11, mark, new VBox(1, brandName, brandTag));
        brand.setAlignment(Pos.CENTER_LEFT);

        Label badge = new Label("ONE-TIME SECURITY SETUP");
        badge.getStyleClass().add("auth-badge");

        Label title = new Label("Set up the administrator");
        title.getStyleClass().add("auth-title");
        Label subtitle = new Label("No administrator account exists yet. Choose the credentials"
                + " now; the password is salted and hashed before it is stored.");
        subtitle.getStyleClass().add("auth-subtitle");
        subtitle.setWrapText(true);

        Label notice = new Label("Security note: this screen stops working as soon as one"
                + " administrator exists. No default password is shipped with the application.");
        notice.getStyleClass().add("notice-box");
        notice.setWrapText(true);

        createButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setMaxWidth(Double.MAX_VALUE);

        VBox card = new VBox(12,
                brand,
                badge,
                new VBox(5, title, subtitle),
                notice,
                UiFactory.field("Full name", nameField),
                UiFactory.field("Email address", emailField),
                UiFactory.field("Password", passwordField),
                UiFactory.field("Confirm password", confirmField),
                message,
                createButton,
                UiFactory.separator(),
                backButton);
        card.getStyleClass().add("auth-card");
        card.setPrefWidth(460);
        card.setMaxWidth(490);
        card.setMinWidth(330);
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

    public Button getCreateButton() {
        return createButton;
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
