package com.onlinequiz.view;

import com.onlinequiz.model.Question;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Timed quiz attempt. The view renders one question at a time and reports option
 * selections back to the controller; all scoring and persistence stay outside the
 * view.
 */
public final class QuizAttemptView extends VBox {

    private final Label quizTitle = UiFactory.pageTitle("Quiz attempt");
    private final Label timerLabel = new Label("--:--");
    private final Label timerCaption = new Label("Time remaining");
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label progressText = new Label("Answered 0 of 0");
    private final Label questionNumber = new Label();
    private final Label questionText = new Label();
    private final RadioButton[] options = new RadioButton[4];
    private final ToggleGroup optionGroup = new ToggleGroup();
    private final Button previousButton = UiFactory.secondaryButton("← Previous");
    private final Button nextButton = UiFactory.secondaryButton("Next →");
    private final Button submitButton = UiFactory.primaryButton("Submit attempt");
    private final Label message = UiFactory.messageLabel();
    private final ScrollPane questionScroll;

    private Consumer<String> onOptionSelected = label -> {
    };
    private boolean updatingSelection;

    public QuizAttemptView() {
        getStyleClass().add("attempt-root");
        setSpacing(14);
        setPadding(new Insets(2));
        setFocusTraversable(true);
        setMinWidth(0);

        quizTitle.setWrapText(true);
        quizTitle.setMinWidth(0);

        timerLabel.getStyleClass().add("timer-badge");
        timerCaption.getStyleClass().add("metric-caption");

        Label eyebrow = new Label("QUIZ IN PROGRESS");
        eyebrow.getStyleClass().add("hero-eyebrow");

        VBox titleBox = new VBox(3, eyebrow, quizTitle);
        titleBox.setMinWidth(0);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        VBox timerBox = new VBox(3, timerCaption, timerLabel);
        timerBox.setAlignment(Pos.CENTER_RIGHT);

        HBox headerTop = new HBox(16, titleBox, timerBox);
        headerTop.setAlignment(Pos.CENTER_LEFT);

        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.getStyleClass().add("quiz-progress");
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        progressText.getStyleClass().add("muted-text");
        HBox progressRow = new HBox(12, progressBar, progressText);
        progressRow.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(12, headerTop, progressRow);
        header.getStyleClass().add("attempt-header");

        questionNumber.getStyleClass().add("question-number");
        questionText.getStyleClass().add("question-text");
        questionText.setWrapText(true);

        Label typeBadge = UiFactory.badge("SINGLE CHOICE", "badge-blue");
        HBox questionHeader = new HBox(10, questionNumber, UiFactory.spacer(), typeBadge);
        questionHeader.setAlignment(Pos.CENTER_LEFT);

        VBox optionsBox = new VBox(10);
        optionsBox.getStyleClass().add("options-box");
        String[] labels = {"A", "B", "C", "D"};
        for (int index = 0; index < options.length; index++) {
            RadioButton option = new RadioButton();
            option.setToggleGroup(optionGroup);
            option.setUserData(labels[index]);
            option.setMaxWidth(Double.MAX_VALUE);
            option.setWrapText(true);
            option.getStyleClass().add("option-radio");
            options[index] = option;
            optionsBox.getChildren().add(option);
        }

        optionGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (updatingSelection || newValue == null) {
                return;
            }
            Object label = newValue.getUserData();
            if (label != null) {
                onOptionSelected.accept(String.valueOf(label));
            }
        });

        VBox questionCard = new VBox(14, questionHeader, questionText, optionsBox);
        questionCard.getStyleClass().add("question-card");
        questionCard.setMinWidth(0);

        questionScroll = UiFactory.scroll(questionCard);
        questionScroll.setFitToHeight(false);
        questionScroll.setPrefViewportHeight(400);
        questionScroll.setMinHeight(0);
        VBox.setVgrow(questionScroll, Priority.ALWAYS);

        previousButton.setPrefWidth(124);
        previousButton.setMaxWidth(150);
        nextButton.setPrefWidth(110);
        nextButton.setMaxWidth(150);
        submitButton.setPrefWidth(210);
        submitButton.setMaxWidth(240);
        HBox navigation = new HBox(10, previousButton, nextButton,
                UiFactory.spacer(), submitButton);
        navigation.setAlignment(Pos.CENTER_LEFT);

        Label hint = new Label("Keyboard: ← previous  •  → next  •  1-4 choose an answer");
        hint.getStyleClass().add("shortcut-hint");
        hint.setWrapText(true);

        VBox footer = new VBox(10, message, navigation, hint);
        footer.getStyleClass().add("attempt-header");

        getChildren().addAll(header, questionScroll, footer);
    }

    public void setQuizTitle(String title) {
        quizTitle.setText(title);
    }

    public void setOnOptionSelected(Consumer<String> handler) {
        this.onOptionSelected = handler == null ? label -> {
        } : handler;
    }

    public void renderQuestion(Question question, int index, int total, String selectedAnswer) {
        updatingSelection = true;
        questionNumber.setText("QUESTION " + (index + 1) + " OF " + total);
        questionText.setText(question.getQuestionText());
        String[] labels = {"A", "B", "C", "D"};
        String[] values = question.getOptions();
        for (int optionIndex = 0; optionIndex < options.length; optionIndex++) {
            options[optionIndex].setText(labels[optionIndex] + ".   " + values[optionIndex]);
            boolean selected = selectedAnswer != null
                    && labels[optionIndex].equals(selectedAnswer);
            options[optionIndex].setSelected(selected);
        }
        updatingSelection = false;
        questionScroll.setVvalue(0);
    }

    public void setTimer(String text) {
        timerLabel.setText(text);
        int seconds = parseSeconds(text);
        boolean warning = seconds >= 0 && seconds <= 60;
        if (warning) {
            if (!timerLabel.getStyleClass().contains("timer-warning")) {
                timerLabel.getStyleClass().add("timer-warning");
            }
        } else {
            timerLabel.getStyleClass().remove("timer-warning");
        }
    }

    public void setProgress(int answered, int total) {
        double value = total <= 0 ? 0.0 : (double) answered / total;
        progressBar.setProgress(value);
        progressText.setText("Answered " + answered + " of " + total);
    }

    public void setNavigationEnabled(boolean previous, boolean next) {
        previousButton.setDisable(!previous);
        nextButton.setDisable(!next);
    }

    public void setSubmitting(boolean submitting) {
        previousButton.setDisable(submitting);
        nextButton.setDisable(submitting);
        submitButton.setDisable(submitting);
        for (RadioButton option : options) {
            option.setDisable(submitting);
        }
        submitButton.setText(submitting ? "Submitting..." : "Submit attempt");
    }

    public void selectOption(int index) {
        if (index < 0 || index >= options.length) {
            return;
        }
        if (!options[index].isDisabled()) {
            options[index].setSelected(true);
        }
    }

    public String getSelectedAnswer() {
        if (!(optionGroup.getSelectedToggle() instanceof RadioButton)) {
            return null;
        }
        Object label = optionGroup.getSelectedToggle().getUserData();
        return label == null ? null : String.valueOf(label);
    }

    public Button getPreviousButton() {
        return previousButton;
    }

    public Button getNextButton() {
        return nextButton;
    }

    public Button getSubmitButton() {
        return submitButton;
    }

    public void setError(String text) {
        UiFactory.error(message, text);
    }

    public void clearMessage() {
        UiFactory.clear(message);
    }

    private static int parseSeconds(String text) {
        if (text == null) {
            return -1;
        }
        String[] parts = text.trim().split(":");
        if (parts.length != 2) {
            return -1;
        }
        try {
            return Integer.parseInt(parts[0].trim()) * 60
                    + Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException error) {
            return -1;
        }
    }
}
