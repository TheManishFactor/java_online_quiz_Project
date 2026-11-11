package com.onlinequiz.view;

import com.onlinequiz.model.Result;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Post-attempt result summary with grade, timing and a full answer review.
 */
public final class ResultView extends VBox {

    /**
     * One row in the answer review.
     */
    public static final class ReviewItem {

        private final String question;
        private final String selected;
        private final String correct;
        private final boolean correctAnswer;

        public ReviewItem(String question, String selected, String correct, boolean correctAnswer) {
            this.question = question;
            this.selected = selected;
            this.correct = correct;
            this.correctAnswer = correctAnswer;
        }

        public String getQuestion() {
            return question;
        }

        public String getSelected() {
            return selected;
        }

        public String getCorrect() {
            return correct;
        }

        public boolean isCorrectAnswer() {
            return correctAnswer;
        }
    }

    private final Label pageSubtitle = UiFactory.pageSubtitle("");
    private final Label percentageCaption = UiFactory.muted("PERCENTAGE");
    private final Label percentageLabel = new Label("-");
    private final Label gradeLabel = new Label("-");
    private final Label outcomeLabel = new Label();
    private final Label scoreLabel = new Label("-");
    private final Label correctLabel = new Label("-");
    private final Label incorrectLabel = new Label("-");
    private final Label timeLabel = new Label("-");
    private final Label reviewCount = new Label();
    private final VBox reviewBox = new VBox(10);

    private final Button retakeButton = UiFactory.primaryButton("Retake quiz");
    private final Button backButton = UiFactory.secondaryButton("Back to dashboard");

    public ResultView() {
        setSpacing(18);
        setPadding(new Insets(2));

        HBox header = new HBox(16,
                UiFactory.pageHeader("ATTEMPT COMPLETE", "Attempt result", ""),
                UiFactory.spacer(), backButton, retakeButton);
        header.setAlignment(Pos.CENTER_LEFT);

        percentageLabel.getStyleClass().add("result-percentage");
        gradeLabel.getStyleClass().add("result-grade");
        outcomeLabel.getStyleClass().addAll("badge", "badge-pass");

        VBox scoreBlock = new VBox(4, percentageLabel, percentageCaption, gradeLabel, outcomeLabel);
        scoreBlock.setAlignment(Pos.CENTER);
        scoreBlock.getStyleClass().add("result-score-block");

        VBox metrics = new VBox(10,
                metric("Score", scoreLabel),
                metric("Correct answers", correctLabel),
                metric("Incorrect answers", incorrectLabel),
                metric("Time taken", timeLabel));
        metrics.setMinWidth(0);
        HBox.setHgrow(metrics, Priority.ALWAYS);

        HBox hero = new HBox(26, scoreBlock, metrics);
        hero.getStyleClass().add("result-hero");
        hero.setAlignment(Pos.CENTER_LEFT);
        scoreBlock.setMinWidth(0);
        scoreBlock.setPrefWidth(210);

        reviewCount.getStyleClass().add("muted-text");
        HBox reviewHeader = new HBox(12,
                UiFactory.sectionHeader("Answer review",
                        "Every question with your answer and the correct option."),
                UiFactory.spacer(), reviewCount);
        reviewHeader.setAlignment(Pos.CENTER_LEFT);

        reviewBox.getStyleClass().add("review-list");
        VBox review = UiFactory.card(reviewHeader, reviewBox);

        getChildren().addAll(header, pageSubtitle, hero, review);
    }

    public Button getRetakeButton() {
        return retakeButton;
    }

    public Button getBackButton() {
        return backButton;
    }

    public void setSummary(String studentName, String quizTitle, Result result, long seconds) {
        pageSubtitle.setText(studentName + "  •  " + quizTitle);
        percentageLabel.setText(result.getPercentage() + "%");
        gradeLabel.setText("Grade " + result.getGrade());
        scoreLabel.setText(result.getScoreLabel());
        correctLabel.setText(String.valueOf(result.getScore()));
        incorrectLabel.setText(String.valueOf(result.getTotalQuestions() - result.getScore()));
        timeLabel.setText(formatDuration(seconds));

        outcomeLabel.setText(result.isPassed() ? "PASSED" : "NOT PASSED");
        outcomeLabel.getStyleClass().removeAll("badge-pass", "badge-fail");
        outcomeLabel.getStyleClass().add(result.isPassed() ? "badge-pass" : "badge-fail");
    }

    public void setReview(List<ReviewItem> items) {
        reviewBox.getChildren().clear();
        if (items == null || items.isEmpty()) {
            reviewCount.setText("");
            reviewBox.getChildren().add(UiFactory.emptyState("No answers to review."));
            return;
        }
        long correctCount = items.stream().filter(ReviewItem::isCorrectAnswer).count();
        reviewCount.setText(correctCount + " correct  •  "
                + (items.size() - correctCount) + " incorrect");

        int index = 0;
        for (ReviewItem item : items) {
            index++;
            Label number = new Label(String.format("%02d", index));
            number.getStyleClass().add("review-index");

            Label status = UiFactory.badge(item.isCorrectAnswer() ? "CORRECT" : "INCORRECT",
                    item.isCorrectAnswer() ? "badge-pass" : "badge-fail");
            Label question = new Label(item.getQuestion());
            question.getStyleClass().add("review-question");
            question.setWrapText(true);

            String selectedText = item.getSelected() == null || item.getSelected().isEmpty()
                    ? "Not answered"
                    : item.getSelected();
            Label answers = new Label("Your answer: " + selectedText
                    + "    •    Correct answer: " + item.getCorrect());
            answers.getStyleClass().add("review-answers");
            answers.setWrapText(true);

            VBox text = new VBox(4, question, answers);
            text.setMinWidth(0);
            HBox.setHgrow(text, Priority.ALWAYS);
            HBox row = new HBox(12, number, status, text);
            row.setAlignment(Pos.TOP_LEFT);
            row.getStyleClass().add(item.isCorrectAnswer() ? "review-row-correct"
                    : "review-row-wrong");
            reviewBox.getChildren().add(row);
        }
    }

    public static String formatDuration(long seconds) {
        long safe = Math.max(0, seconds);
        long minutes = safe / 60;
        long remainder = safe % 60;
        return String.format("%02d:%02d", minutes, remainder);
    }

    private HBox metric(String caption, Label value) {
        Label captionLabel = new Label(caption);
        captionLabel.getStyleClass().add("metric-caption");
        value.getStyleClass().add("metric-value");
        HBox row = new HBox(10, captionLabel, UiFactory.spacer(), value);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("metric-row");
        return row;
    }
}
