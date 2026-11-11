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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Administrator overview with platform-wide counters, real recent activity and
 * shortcuts to the five management areas.
 */
public final class AdminDashboardView extends VBox {

    private final Label avatar = new Label("A");
    private final Label greeting = new Label("Administrator dashboard");
    private final Label subtitle = new Label("Loading platform statistics...");
    private final Label activityChip = new Label("Loading activity");

    private final StatCard studentsCard = new StatCard("◎", "Students", "-",
            "Registered accounts", "accent-indigo");
    private final StatCard quizzesCard = new StatCard("▤", "Quizzes", "-",
            "Across all categories", "accent-violet");
    private final StatCard questionsCard = new StatCard("✎", "Questions", "-",
            "Four-option questions", "accent-sky");
    private final StatCard attemptsCard = new StatCard("◷", "Attempts", "-",
            "Graded results stored", "accent-green");

    private final Label averageValue = new Label("-");
    private final Label passRateValue = new Label("-");
    private final Label bestValue = new Label("-");
    private final Label passedValue = new Label("-");
    private final ProgressBar averageBar = new ProgressBar(0);
    private final VBox recentBox = new VBox(8);

    private final Button manageQuizzesButton = UiFactory.primaryButton("Manage quizzes");
    private final Button manageQuestionsButton = UiFactory.secondaryButton("Manage questions");
    private final Button manageStudentsButton = UiFactory.secondaryButton("Manage students");
    private final Button viewResultsButton = UiFactory.secondaryButton("View results");
    private final Button analyticsButton = UiFactory.secondaryButton("Analytics");
    private final Button refreshButton = UiFactory.ghostButton("Refresh");
    private final Button logoutButton = UiFactory.ghostButton("Sign out");

    public AdminDashboardView() {
        setSpacing(18);
        setPadding(new Insets(2));
        setMinWidth(0);

        avatar.getStyleClass().addAll("avatar", "avatar-lg");
        greeting.getStyleClass().add("welcome-title");
        greeting.setWrapText(true);
        subtitle.getStyleClass().add("welcome-subtitle");
        subtitle.setWrapText(true);

        Label eyebrow = new Label("ADMIN WORKSPACE");
        eyebrow.getStyleClass().add("hero-eyebrow");
        VBox greetingBox = new VBox(3, eyebrow, greeting, subtitle);
        greetingBox.setMinWidth(0);
        HBox.setHgrow(greetingBox, Priority.ALWAYS);

        Label dot = new Label();
        dot.getStyleClass().add("notification-dot");
        activityChip.getStyleClass().add("text-soft");
        HBox status = new HBox(8, dot, activityChip);
        status.setAlignment(Pos.CENTER_LEFT);
        status.getStyleClass().add("role-pill");

        GridPane actionsGrid = UiFactory.columns(8, refreshButton, logoutButton);
        VBox headerActions = new VBox(10, status, actionsGrid);
        headerActions.setAlignment(Pos.CENTER_RIGHT);
        headerActions.setMinWidth(180);

        HBox welcome = new HBox(16, avatar, greetingBox, headerActions);
        welcome.setAlignment(Pos.CENTER_LEFT);
        welcome.getStyleClass().add("welcome-strip");
        welcome.setMinWidth(0);

        GridPane stats = UiFactory.statGrid(studentsCard, quizzesCard, questionsCard, attemptsCard);

        averageValue.getStyleClass().add("stat-value");
        averageBar.setMaxWidth(Double.MAX_VALUE);
        averageBar.getStyleClass().add("quiz-progress");
        GridPane.setHgrow(averageBar, Priority.ALWAYS);

        VBox averageBlock = new VBox(5,
                caption("Platform average"),
                averageValue,
                averageBar,
                UiFactory.muted("Mean percentage across every graded attempt."));

        GridPane metrics = UiFactory.columns(10,
                metricTile("Pass rate", passRateValue),
                metricTile("Best score", bestValue),
                metricTile("Passed attempts", passedValue));

        VBox performancePanel = UiFactory.card(
                UiFactory.sectionHeader("Performance overview",
                        "Aggregated from real student attempts"),
                averageBlock,
                metrics);

        FlowPane actions = new FlowPane(8, 8,
                manageQuizzesButton, manageQuestionsButton, manageStudentsButton,
                viewResultsButton, analyticsButton);
        actions.getStyleClass().add("action-row");

        VBox adminPanel = UiFactory.card(
                UiFactory.sectionHeader("Administration",
                        "Create quizzes, author questions and review performance"),
                actions,
                UiFactory.muted("Student accounts never reach these screens, and passwords are"
                        + " never displayed in management tables or exported reports."));

        VBox leftColumn = new VBox(16, performancePanel, adminPanel);
        leftColumn.setMinWidth(0);

        recentBox.getStyleClass().add("recent-list");
        VBox recentPanel = UiFactory.card(
                UiFactory.sectionHeader("Recent activity",
                        "The five latest graded attempts"),
                recentBox);

        VBox securityPanel = UiFactory.card(
                UiFactory.sectionHeader("Data & security",
                        "How the platform protects accounts"),
                UiFactory.muted("Passwords are stored as salted PBKDF2 hashes. Authentication"
                        + " and role-based navigation are enforced on every screen."));

        VBox rightColumn = new VBox(16, recentPanel, securityPanel);
        rightColumn.setMinWidth(0);

        GridPane body = UiFactory.split(leftColumn, rightColumn, 58);

        getChildren().addAll(welcome, stats, body);
    }

    public Button getManageQuizzesButton() {
        return manageQuizzesButton;
    }

    public Button getManageQuestionsButton() {
        return manageQuestionsButton;
    }

    public Button getManageStudentsButton() {
        return manageStudentsButton;
    }

    public Button getViewResultsButton() {
        return viewResultsButton;
    }

    public Button getAnalyticsButton() {
        return analyticsButton;
    }

    public Button getRefreshButton() {
        return refreshButton;
    }

    public Button getLogoutButton() {
        return logoutButton;
    }

    public void setWelcome(String adminName) {
        String name = adminName == null || adminName.isBlank() ? "Administrator" : adminName.trim();
        greeting.setText("Administrator dashboard");
        subtitle.setText("Signed in as " + name + ". Here is the current platform state.");
        avatar.setText(name.substring(0, 1).toUpperCase());
    }

    public void setStats(int students, int quizzes, int questions, int attempts, double average) {
        studentsCard.setValue(String.valueOf(students));
        studentsCard.setDescription(students == 1 ? "One student account"
                : students + " student accounts");
        quizzesCard.setValue(String.valueOf(quizzes));
        questionsCard.setValue(String.valueOf(questions));
        attemptsCard.setValue(String.valueOf(attempts));
        attemptsCard.setDescription(attempts == 0 ? "No attempts yet"
                : attempts + " graded results");
        averageValue.setText(String.format("%.1f%%", average));
        averageBar.setProgress(Math.max(0, Math.min(1, average / 100.0)));
        activityChip.setText(attempts == 0 ? "No attempts recorded"
                : attempts + (attempts == 1 ? " attempt recorded" : " attempts recorded"));
    }

    public void setRecentActivity(List<Result> results, Map<Integer, String> studentNames,
                                  Map<Integer, String> quizTitles) {
        recentBox.getChildren().clear();
        if (results == null || results.isEmpty()) {
            passRateValue.setText("0%");
            bestValue.setText("-");
            passedValue.setText("0 of 0");
            recentBox.getChildren().add(UiFactory.emptyState(
                    "No attempts have been submitted yet."));
            return;
        }

        int passed = 0;
        double best = 0;
        for (Result result : results) {
            if (result.isPassed()) {
                passed++;
            }
            best = Math.max(best, result.getPercentage());
        }
        int rate = (int) Math.round(passed * 100.0 / results.size());
        passRateValue.setText(rate + "%");
        bestValue.setText(String.format("%.1f%%", best));
        passedValue.setText(passed + " of " + results.size());

        List<Result> ordered = new ArrayList<>(results);
        ordered.sort(Comparator.comparingInt(Result::getId).reversed());
        int shown = 0;
        for (Result result : ordered) {
            if (shown++ >= 5) {
                break;
            }
            recentBox.getChildren().add(activityRow(result, studentNames, quizTitles));
        }
    }

    private HBox activityRow(Result result, Map<Integer, String> studentNames,
                             Map<Integer, String> quizTitles) {
        String student = studentNames == null ? "Student " + result.getUserId()
                : studentNames.getOrDefault(result.getUserId(), "Student " + result.getUserId());
        String quiz = quizTitles == null ? "Quiz " + result.getQuizId()
                : quizTitles.getOrDefault(result.getQuizId(), "Quiz " + result.getQuizId());

        Label icon = new Label(result.isPassed() ? "✓" : "!");
        icon.getStyleClass().addAll("activity-icon",
                result.isPassed() ? "accent-green" : "accent-amber");

        Label title = new Label(student + "  •  " + quiz);
        title.getStyleClass().add("recent-title");
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);

        Label detail = new Label(result.getScoreLabel() + "  •  "
                + result.getPercentage() + "%  •  " + result.getAttemptDate());
        detail.getStyleClass().add("recent-score");
        detail.setWrapText(true);

        VBox text = new VBox(2, title, detail);
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
}
