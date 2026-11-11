package com.onlinequiz.view;

import com.onlinequiz.model.Quiz;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

/**
 * Catalog of available quizzes shown as cards, loaded asynchronously by the
 * controller.
 */
public final class QuizSelectionView extends VBox {

    private final Button refreshButton = UiFactory.ghostButton("Refresh");
    private final Label status = new Label("Loading quizzes...");
    private final ProgressIndicator loading = new ProgressIndicator();
    private final FlowPane cards = new FlowPane(16, 16);
    private final ScrollPane scroll;

    public QuizSelectionView() {
        setSpacing(16);
        setPadding(new Insets(2));

        HBox header = new HBox(16,
                UiFactory.pageHeader("QUIZ LIBRARY", "Available quizzes",
                        "Choose a quiz to begin. The timer starts as soon as the attempt opens."),
                UiFactory.spacer(), refreshButton);
        header.setAlignment(Pos.CENTER_LEFT);

        status.getStyleClass().add("muted-text");
        status.setWrapText(true);
        loading.setPrefSize(28, 28);
        loading.setVisible(false);
        loading.setManaged(false);

        HBox statusRow = new HBox(12, status, loading);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        cards.getStyleClass().add("quiz-grid");
        cards.setPadding(new Insets(2, 2, 14, 2));
        scroll = UiFactory.scroll(cards);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(header, statusRow, scroll);
    }

    public Button getRefreshButton() {
        return refreshButton;
    }

    public void showLoading() {
        plainStatus("Loading quizzes...");
        loading.setVisible(true);
        loading.setManaged(true);
        cards.getChildren().clear();
    }

    public void showError(String message) {
        loading.setVisible(false);
        loading.setManaged(false);
        plainStatus("");
        UiFactory.error(status, message);
        cards.getChildren().clear();
    }

    public void showQuizzes(List<Quiz> quizzes, Consumer<Quiz> onStart) {
        loading.setVisible(false);
        loading.setManaged(false);
        cards.getChildren().clear();

        if (quizzes == null || quizzes.isEmpty()) {
            plainStatus("No quizzes available");
            cards.getChildren().add(UiFactory.emptyState(
                    "The catalog is empty. An administrator can create the first quiz."));
            return;
        }

        plainStatus(quizzes.size() + (quizzes.size() == 1
                ? " quiz ready to attempt" : " quizzes ready to attempt"));
        for (Quiz quiz : quizzes) {
            cards.getChildren().add(new QuizCard(quiz, () -> onStart.accept(quiz)));
        }
    }

    private void plainStatus(String text) {
        status.getStyleClass().setAll("muted-text");
        status.setText(text);
    }
}
