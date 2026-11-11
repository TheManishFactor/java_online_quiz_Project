package com.onlinequiz.view;

import com.onlinequiz.model.Quiz;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Attractive quiz summary card used by the quiz selection screen.
 */
public final class QuizCard extends VBox {

    public QuizCard(Quiz quiz, Runnable onStart) {
        getStyleClass().add("quiz-card");
        setSpacing(10);
        setPadding(new Insets(20));
        setPrefWidth(320);
        setMinWidth(270);
        setMaxWidth(Double.MAX_VALUE);

        Label category = UiFactory.badge(quiz.getCategory(), "badge-purple");
        Label availability = UiFactory.badge(quiz.hasQuestions() ? "READY" : "NEEDS QUESTIONS",
                quiz.hasQuestions() ? "badge-pass" : "badge-warning");
        HBox badges = new HBox(8, category, availability);
        badges.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(quiz.getTitle());
        title.getStyleClass().add("quiz-card-title");
        title.setWrapText(true);

        HBox questions = metaRow("▤", quiz.getTotalQuestions() + " questions");
        HBox minutes = metaRow("◷", quiz.getDuration() + " minutes");
        HBox meta = new HBox(16, questions, minutes);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label hint = new Label(quiz.hasQuestions()
                ? "The timer starts as soon as the attempt opens."
                : "Add questions before this quiz can start.");
        hint.getStyleClass().add("quiz-card-hint");
        hint.setWrapText(true);

        Button start = UiFactory.primaryButton("Start quiz");
        start.setMaxWidth(Double.MAX_VALUE);
        start.setDisable(!quiz.hasQuestions());
        start.setOnAction(event -> onStart.run());

        HBox footer = new HBox(start);
        HBox.setHgrow(start, Priority.ALWAYS);

        getChildren().addAll(badges, title, meta, hint, UiFactory.spacer(),
                UiFactory.separator(), footer);
        UiFactory.addHoverLift(this);
    }

    private HBox metaRow(String icon, String text) {
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("nav-icon");
        Label label = new Label(text);
        label.getStyleClass().add("quiz-card-meta");
        HBox row = new HBox(6, iconLabel, label);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}
