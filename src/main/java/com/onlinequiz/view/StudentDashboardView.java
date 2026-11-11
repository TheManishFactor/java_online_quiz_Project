package com.onlinequiz.view;

import com.onlinequiz.model.Result;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Student overview: personal greeting, performance counters, categories and the
 * most recent attempts, with shortcuts to every student workflow.
 */
public final class StudentDashboardView extends VBox {

    private final Label avatar = new Label("S");
    private final Label greeting = new Label("Student dashboard");
    private final Label subtitle = new Label("Loading your latest quiz activity...");
    private final Label attemptsChip = new Label("Loading activity");

    private final StatCard quizzesCard = new StatCard("▤", "Available quizzes", "-",
            "Published and ready", "accent-indigo");
    private final StatCard attemptsCard = new StatCard("◷", "Total attempts", "-",
            "Graded automatically", "accent-violet");
    private final StatCard averageCard = new StatCard("◔", "Average score", "-",
            "Across all attempts", "accent-sky");
    private final StatCard completedCard = new StatCard("✓", "Completed quizzes", "-",
            "Unique quizzes finished", "accent-green");

    private final FlowPane categoryChips = new FlowPane(8, 8);
    private final Label categorySubtitle = new Label("Loading categories...");
    private final Label averageValue = new Label("-");
    private final Label passedValue = new Label("-");
    private final Label bestValue = new Label("-");
    private final ProgressBar averageBar = new ProgressBar(0);
    private final VBox recentBox = new VBox(8);
    private final Label recommendationText = new Label("");

    private final Button startQuizButton = UiFactory.primaryButton("Start a quiz");
    private final Button quizzesButton = UiFactory.secondaryButton("Available quizzes");
    private final Button resultsButton = UiFactory.secondaryButton("My results");
    private final Button profileButton = UiFactory.secondaryButton("My profile");
    private final Button logoutButton = UiFactory.ghostButton("Sign out");

    private int quizCount;
    private int categoryCount;
    private int attemptCount;
    private double averageScore;

    public StudentDashboardView() {
        setSpacing(18);
        setPadding(new Insets(2));
        setMinWidth(0);

        avatar.getStyleClass().addAll("avatar", "avatar-lg");

        greeting.getStyleClass().add("welcome-title");
        greeting.setWrapText(true);
        subtitle.getStyleClass().add("welcome-subtitle");
        subtitle.setWrapText(true);

        Label eyebrow = new Label("STUDENT WORKSPACE");
        eyebrow.getStyleClass().add("hero-eyebrow");
        VBox greetingBox = new VBox(3, eyebrow, greeting, subtitle);
        greetingBox.setMinWidth(0);
        HBox.setHgrow(greetingBox, Priority.ALWAYS);

        Label dot = new Label();
        dot.getStyleClass().add("notification-dot");
        attemptsChip.getStyleClass().add("text-soft");
        HBox status = new HBox(8, dot, attemptsChip);
        status.setAlignment(Pos.CENTER_LEFT);
        status.getStyleClass().add("role-pill");

        startQuizButton.setPrefWidth(180);
        startQuizButton.setMaxWidth(200);
        VBox actions = new VBox(10, status, startQuizButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        HBox welcome = new HBox(16, avatar, greetingBox, actions);
        welcome.setAlignment(Pos.CENTER_LEFT);
        welcome.getStyleClass().add("welcome-strip");
        welcome.setMinWidth(0);

        GridPane stats = UiFactory.statGrid(quizzesCard, attemptsCard, averageCard, completedCard);

        averageValue.getStyleClass().add("stat-value");
        averageBar.setMaxWidth(Double.MAX_VALUE);
        averageBar.getStyleClass().add("quiz-progress");
        GridPane.setHgrow(averageBar, Priority.ALWAYS);

        VBox averageBlock = new VBox(5,
                caption("Average score"),
                averageValue,
                averageBar,
                UiFactory.muted("Calculated from every graded attempt."));

        GridPane metrics = UiFactory.columns(10,
                metricTile("Attempts passed", passedValue),
                metricTile("Best score", bestValue));

        VBox performancePanel = UiFactory.card(
                UiFactory.sectionHeader("Performance overview",
                        "Real scores from your recorded attempts"),
                averageBlock,
                metrics);

        categoryChips.getStyleClass().add("chip-row");
        categorySubtitle.getStyleClass().add("section-subtitle");
        categorySubtitle.setWrapText(true);
        VBox categoryPanel = UiFactory.card(
                new VBox(3, UiFactory.sectionTitle("Quiz categories"), categorySubtitle),
                categoryChips);

        VBox leftColumn = new VBox(16, performancePanel, categoryPanel);
        leftColumn.setMinWidth(0);

        recentBox.getStyleClass().add("recent-list");
        VBox recentPanel = UiFactory.card(
                UiFactory.sectionHeader("Recent results",
                        "Your five latest graded attempts"),
                recentBox);

        FlowPane quickActions = new FlowPane(8, 8);
        quickActions.getStyleClass().add("action-row");
        quickActions.getChildren().addAll(quizzesButton, resultsButton, profileButton, logoutButton);
        VBox actionsPanel = UiFactory.card(
                UiFactory.sectionTitle("Quick actions"),
                quickActions);

        Label recommendationIcon = new Label("✦");
        recommendationIcon.getStyleClass().addAll("stat-icon", "accent-amber");
        HBox recommendationHeader = new HBox(10, recommendationIcon,
                UiFactory.sectionTitle("Recommended next step"));
        recommendationHeader.setAlignment(Pos.CENTER_LEFT);

        recommendationText.getStyleClass().add("muted-text");
        recommendationText.setWrapText(true);

        Button browseButton = UiFactory.secondaryButton("Browse quizzes");
        browseButton.setOnAction(event -> startQuizButton.fire());

        VBox recommendationPanel = UiFactory.card(
                recommendationHeader,
                recommendationText,
                browseButton);

        VBox rightColumn = new VBox(16, recentPanel, actionsPanel, recommendationPanel);
        rightColumn.setMinWidth(0);

        GridPane body = UiFactory.split(leftColumn, rightColumn, 57);

        getChildren().addAll(welcome, stats, body);
    }

    public Button getStartQuizButton() {
        return startQuizButton;
    }

    public Button getQuizzesButton() {
        return quizzesButton;
    }

    public Button getResultsButton() {
        return resultsButton;
    }

    public Button getProfileButton() {
        return profileButton;
    }

    public Button getLogoutButton() {
        return logoutButton;
    }

    public void setWelcome(String studentName) {
        String name = studentName == null || studentName.isBlank() ? "there" : studentName.trim();
        greeting.setText("Welcome back, " + name);
        subtitle.setText("Here is your learning snapshot, built from your real quiz activity.");
        avatar.setText(name.substring(0, 1).toUpperCase());
    }

    public void setStats(int quizzes, int attempts, double average, int categories) {
        quizCount = quizzes;
        attemptCount = attempts;
        averageScore = average;
        categoryCount = categories;

        quizzesCard.setValue(String.valueOf(quizzes));
        quizzesCard.setDescription(quizzes == 0 ? "None published yet" : "Published and ready");
        attemptsCard.setValue(String.valueOf(attempts));
        attemptsCard.setDescription(attempts == 0 ? "No attempts yet" : "Graded automatically");
        averageCard.setValue(String.format("%.1f%%", average));
        averageCard.setDescription(attempts == 0 ? "No attempts yet"
                : "Across " + attempts + (attempts == 1 ? " attempt" : " attempts"));
        averageValue.setText(String.format("%.1f%%", average));
        averageBar.setProgress(Math.max(0, Math.min(1, average / 100.0)));

        attemptsChip.setText(attempts == 0 ? "No attempts recorded"
                : attempts + (attempts == 1 ? " attempt recorded" : " attempts recorded"));
        categorySubtitle.setText(categories == 0 ? "No categories available yet"
                : categories + (categories == 1 ? " category available" : " categories available"));
        updateRecommendation();
    }

    public void setCategories(Collection<String> categories) {
        categoryChips.getChildren().clear();
        if (categories == null || categories.isEmpty()) {
            categoryChips.getChildren().add(UiFactory.muted("No categories yet."));
            return;
        }
        for (String category : categories) {
            categoryChips.getChildren().add(UiFactory.chip(category));
        }
    }

    public void setRecentResults(List<Result> results, Map<Integer, String> quizTitles) {
        recentBox.getChildren().clear();
        if (results == null || results.isEmpty()) {
            completedCard.setValue("0");
            passedValue.setText("0 of 0");
            bestValue.setText("-");
            recentBox.getChildren().add(UiFactory.emptyState(
                    "No attempts yet. Start your first quiz to build a result history."));
            updateRecommendation();
            return;
        }

        Set<Integer> completedQuizIds = new HashSet<>();
        int passed = 0;
        double best = 0;
        for (Result result : results) {
            completedQuizIds.add(result.getQuizId());
            if (result.isPassed()) {
                passed++;
            }
            best = Math.max(best, result.getPercentage());
        }
        completedCard.setValue(String.valueOf(completedQuizIds.size()));
        completedCard.setDescription(completedQuizIds.size() == 1
                ? "One quiz completed" : completedQuizIds.size() + " different quizzes");
        passedValue.setText(passed + " of " + results.size());
        bestValue.setText(String.format("%.1f%%", best));

        int shown = 0;
        for (Result result : results) {
            if (shown++ >= 5) {
                break;
            }
            recentBox.getChildren().add(recentRow(result, quizTitles));
        }
        updateRecommendation();
    }

    private HBox recentRow(Result result, Map<Integer, String> quizTitles) {
        String title = quizTitles == null ? "Quiz " + result.getQuizId()
                : quizTitles.getOrDefault(result.getQuizId(), "Quiz " + result.getQuizId());

        Label icon = new Label(result.isPassed() ? "✓" : "!");
        icon.getStyleClass().addAll("activity-icon",
                result.isPassed() ? "accent-green" : "accent-amber");

        Label name = new Label(title);
        name.getStyleClass().add("recent-title");
        name.setWrapText(true);
        name.setMaxWidth(Double.MAX_VALUE);

        Label detail = new Label(result.getScoreLabel() + "  •  "
                + result.getPercentage() + "%  •  " + result.getAttemptDate());
        detail.getStyleClass().add("recent-score");
        detail.setWrapText(true);

        VBox text = new VBox(2, name, detail);
        text.setMinWidth(0);
        HBox.setHgrow(text, Priority.ALWAYS);

        Label outcome = UiFactory.badge(result.isPassed() ? "PASS" : "FAIL",
                result.isPassed() ? "badge-pass" : "badge-fail");

        HBox row = new HBox(10, icon, text, outcome);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("recent-row");
        return row;
    }

    private VBox metricTile(String captionText, Label value) {
        value.getStyleClass().add("metric-value");
        VBox tile = new VBox(4, caption(captionText), value);
        tile.getStyleClass().add("metric-tile");
        UiFactory.fillWidth(tile);
        return tile;
    }

    private Label caption(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("metric-caption");
        return label;
    }

    private void updateRecommendation() {
        if (quizCount == 0) {
            recommendationText.setText("No quizzes are published yet. Check back after your"
                    + " administrator adds one.");
        } else if (attemptCount == 0) {
            recommendationText.setText("Start your first quiz to unlock performance insights"
                    + " and a personal result history.");
        } else if (averageScore >= 80) {
            recommendationText.setText("You are performing strongly. Pick a quiz from another"
                    + " category to keep widening your practice.");
        } else if (averageScore >= 50) {
            recommendationText.setText("You are close to a strong result. A short retake is the"
                    + " quickest way to lift your average.");
        } else {
            recommendationText.setText("Review a recent attempt before your next quiz — the answer"
                    + " review shows exactly which topics to revise.");
        }
        if (categoryCount == 0 && quizCount > 0) {
            recommendationText.setText(recommendationText.getText()
                    + " Categories are still being prepared.");
        }
    }
}
