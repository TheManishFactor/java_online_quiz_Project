package com.onlinequiz.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Landing screen with the product introduction and the two entry points.
 * The first-run administrator setup link is only shown while no admin exists.
 */
public final class WelcomeView extends StackPane {

    private final Button loginButton = UiFactory.primaryButton("Sign in to your account");
    private final Button registerButton = UiFactory.secondaryButton("Create student account");
    private final Button setupButton = UiFactory.ghostButton("Set up the first administrator");
    private final VBox setupBox;

    public WelcomeView() {
        getStyleClass().add("auth-background");
        setPadding(new Insets(34));
        getStyleClass().add("auth-shell");

        VBox hero = new VBox(20);
        hero.setMaxWidth(500);
        hero.setMinWidth(0);
        HBox.setHgrow(hero, Priority.ALWAYS);
        hero.getStyleClass().add("auth-hero");

        Label mark = new Label("Q");
        mark.getStyleClass().add("brand-mark");
        VBox brandText = new VBox(1,
                styledLabel("QUIZ STUDIO", "brand-name"),
                styledLabel("Advanced Java · desktop quiz platform", "brand-tag"));
        HBox brand = new HBox(12, mark, brandText);
        brand.setAlignment(Pos.CENTER_LEFT);

        Label eyebrow = new Label("BUILT WITH JAVAFX & SQLITE");
        eyebrow.getStyleClass().add("hero-eyebrow");

        Label title = new Label("Test your knowledge.\nTrack every attempt.");
        title.getStyleClass().add("hero-title");
        title.setWrapText(true);
        Label description = new Label("A focused desktop quiz platform with timed multiple-choice"
                + " tests, instant grading, answer review and a complete administrator workspace.");
        description.getStyleClass().add("hero-text");
        description.setWrapText(true);

        VBox features = new VBox(12,
                featureRow("Timed MCQ quizzes with automatic submission"),
                featureRow("Instant percentage, grade and answer review"),
                featureRow("Personal history and printable text reports"),
                featureRow("Role-based access for students and administrators"));

        Label stack = new Label("JavaFX 17  •  SQLite JDBC  •  Java 11");
        stack.getStyleClass().add("stack-line");

        hero.getChildren().addAll(brand, eyebrow, title, description, features, stack);

        Label cardMark = new Label("Q");
        cardMark.getStyleClass().add("brand-mark");
        VBox cardBrandText = new VBox(1,
                styledLabel("QUIZ STUDIO", "brand-name"),
                styledLabel("Secure workspace", "brand-tag"));
        HBox cardBrand = new HBox(11, cardMark, cardBrandText);
        cardBrand.setAlignment(Pos.CENTER_LEFT);

        Label cardTitle = new Label("Welcome back");
        cardTitle.getStyleClass().add("auth-title");
        Label cardSubtitle = new Label("Sign in to continue, or create a student account in a few seconds.");
        cardSubtitle.getStyleClass().add("auth-subtitle");
        cardSubtitle.setWrapText(true);

        loginButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setMaxWidth(Double.MAX_VALUE);
        setupButton.setMaxWidth(Double.MAX_VALUE);

        Label setupHint = new Label("No administrator exists yet. Create the first administrator"
                + " securely before managing quizzes.");
        setupHint.getStyleClass().add("muted-text");
        setupHint.setWrapText(true);
        setupBox = new VBox(8, UiFactory.separator(), setupHint, setupButton);
        setupBox.setManaged(false);
        setupBox.setVisible(false);

        Label footnote = new Label("Passwords are stored as salted PBKDF2 hashes.");
        footnote.getStyleClass().add("auth-footnote");
        footnote.setWrapText(true);

        VBox card = new VBox(16,
                cardBrand,
                new VBox(5, cardTitle, cardSubtitle),
                loginButton,
                registerButton,
                setupBox,
                UiFactory.separator(),
                footnote);
        card.getStyleClass().add("auth-card");
        card.setPrefWidth(392);
        card.setMinWidth(320);
        card.setMaxWidth(410);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        HBox content = new HBox(48, hero, card);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(1080);
        getChildren().add(content);
    }

    public Button getLoginButton() {
        return loginButton;
    }

    public Button getRegisterButton() {
        return registerButton;
    }

    public Button getSetupButton() {
        return setupButton;
    }

    public void showAdminSetupLink(boolean show) {
        setupBox.setManaged(show);
        setupBox.setVisible(show);
    }

    private HBox featureRow(String text) {
        Label dot = new Label("✓");
        dot.getStyleClass().add("feature-dot");
        Label label = new Label(text);
        label.getStyleClass().add("feature-text");
        label.setWrapText(true);
        HBox row = new HBox(10, dot, label);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label styledLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }
}
