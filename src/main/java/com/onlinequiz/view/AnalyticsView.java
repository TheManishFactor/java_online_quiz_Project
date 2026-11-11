package com.onlinequiz.view;

import com.onlinequiz.model.Result;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Administrator results and analytics workspace: raw attempts, per-student and
 * per-quiz summaries, plus pass/fail statistics.
 */
public final class AnalyticsView extends VBox {

    public static final class StudentSummary {

        private final int id;
        private final String name;
        private final int attempts;
        private final double average;
        private final double best;
        private final int passed;

        public StudentSummary(int id, String name, int attempts, double average,
                              double best, int passed) {
            this.id = id;
            this.name = name;
            this.attempts = attempts;
            this.average = average;
            this.best = best;
            this.passed = passed;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getAttempts() {
            return attempts;
        }

        public double getAverage() {
            return average;
        }

        public double getBest() {
            return best;
        }

        public int getPassed() {
            return passed;
        }
    }

    public static final class QuizSummary {

        private final int quizId;
        private final String title;
        private final String category;
        private final int attempts;
        private final double average;
        private final double best;

        public QuizSummary(int quizId, String title, String category,
                           int attempts, double average, double best) {
            this.quizId = quizId;
            this.title = title;
            this.category = category;
            this.attempts = attempts;
            this.average = average;
            this.best = best;
        }

        public int getQuizId() {
            return quizId;
        }

        public String getTitle() {
            return title;
        }

        public String getCategory() {
            return category;
        }

        public int getAttempts() {
            return attempts;
        }

        public double getAverage() {
            return average;
        }

        public double getBest() {
            return best;
        }
    }

    private final StatCard attemptsCard = new StatCard("◷", "Total attempts", "-",
            "Graded results", "accent-indigo");
    private final StatCard averageCard = new StatCard("◔", "Average score", "-",
            "Platform-wide mean", "accent-violet");
    private final StatCard highestCard = new StatCard("★", "Highest score", "-",
            "Best attempt", "accent-sky");
    private final StatCard passRateCard = new StatCard("✓", "Pass rate", "-",
            "Passed attempts", "accent-green");
    private final TableView<Result> resultsTable = new TableView<>();
    private final TableView<StudentSummary> studentsTable = new TableView<>();
    private final TableView<QuizSummary> quizzesTable = new TableView<>();
    private final ProgressBar passBar = new ProgressBar(0);
    private final Label passLabel = new Label("Passed: -");
    private final Label failLabel = new Label("Not passed: -");
    private final Label passRateLabel = new Label("-");
    private final TabPane tabs = new TabPane();
    private final Button refreshButton = UiFactory.ghostButton("Refresh");
    private final Label message = UiFactory.messageLabel();

    private Map<Integer, String> studentNames = Collections.emptyMap();
    private Map<Integer, String> quizTitles = Collections.emptyMap();

    public AnalyticsView() {
        setSpacing(16);
        setPadding(new Insets(2));
        setMinWidth(0);

        HBox header = new HBox(16,
                UiFactory.pageHeader("INSIGHTS", "Results and analytics",
                        "All attempts, student-wise performance, quiz-wise performance"
                                + " and pass/fail statistics."),
                UiFactory.spacer(), refreshButton);
        header.setAlignment(Pos.CENTER_LEFT);

        GridPane stats = UiFactory.statGrid(attemptsCard, averageCard,
                highestCard, passRateCard);

        buildResultsTable();
        buildStudentsTable();
        buildQuizzesTable();

        Tab resultsTab = new Tab("All results",
                tabContent("All graded attempts", "Every stored result, newest data first.",
                        resultsTable));
        Tab studentsTab = new Tab("Student summary",
                tabContent("Performance by student", "Attempts, averages and pass counts.",
                        studentsTable));
        Tab quizzesTab = new Tab("Quiz summary",
                tabContent("Performance by quiz", "How each quiz is being attempted.",
                        quizzesTable));
        Tab passFailTab = new Tab("Pass / fail", buildPassFailPanel());
        resultsTab.setClosable(false);
        studentsTab.setClosable(false);
        quizzesTab.setClosable(false);
        passFailTab.setClosable(false);
        tabs.getTabs().addAll(resultsTab, studentsTab, quizzesTab, passFailTab);
        tabs.getStyleClass().add("analytics-tabs");
        tabs.setMinWidth(0);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        getChildren().addAll(header, stats, message, tabs);
    }

    private VBox tabContent(String title, String subtitle, TableView<?> table) {
        HBox heading = new HBox(12,
                UiFactory.sectionHeader(title, subtitle),
                UiFactory.spacer());
        heading.setAlignment(Pos.CENTER_LEFT);
        VBox content = new VBox(12, heading, table);
        content.setMinWidth(0);
        content.setPadding(new Insets(14, 2, 2, 2));
        VBox.setVgrow(table, Priority.ALWAYS);
        return content;
    }

    private VBox buildPassFailPanel() {
        passBar.setMaxWidth(Double.MAX_VALUE);
        passBar.getStyleClass().add("pass-progress");
        passLabel.getStyleClass().add("metric-value");
        failLabel.getStyleClass().add("metric-value");
        passRateLabel.getStyleClass().add("result-percentage");

        HBox counts = new HBox(24, passLabel, failLabel);
        counts.setAlignment(Pos.CENTER_LEFT);

        VBox panel = UiFactory.card(
                UiFactory.sectionHeader("Pass / fail distribution",
                        "A result passes at " + Result.PASS_PERCENTAGE
                                + "% or above."),
                passRateLabel,
                passBar,
                counts,
                UiFactory.muted("Calculated from every attempt stored in the database."));
        panel.setMaxWidth(Double.MAX_VALUE);
        VBox wrapper = new VBox(panel);
        wrapper.setPadding(new Insets(14, 2, 2, 2));
        wrapper.setMinWidth(0);
        return wrapper;
    }

    private void buildResultsTable() {
        styleTable(resultsTable);
        resultsTable.setPlaceholder(UiFactory.emptyState("No attempts recorded yet."));

        TableColumn<Result, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(String.valueOf(data.getValue().getId())));
        idColumn.setPrefWidth(55);

        TableColumn<Result, String> studentColumn = new TableColumn<>("Student");
        studentColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                studentNames.getOrDefault(data.getValue().getUserId(),
                        "User " + data.getValue().getUserId())));
        studentColumn.setPrefWidth(160);

        TableColumn<Result, String> quizColumn = new TableColumn<>("Quiz");
        quizColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                quizTitles.getOrDefault(data.getValue().getQuizId(),
                        "Quiz " + data.getValue().getQuizId())));
        quizColumn.setPrefWidth(190);

        TableColumn<Result, String> scoreColumn = new TableColumn<>("Score");
        scoreColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getScoreLabel()));
        scoreColumn.setPrefWidth(80);

        TableColumn<Result, String> percentColumn = new TableColumn<>("%");
        percentColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getPercentage() + "%"));
        percentColumn.setPrefWidth(75);

        TableColumn<Result, String> gradeColumn = new TableColumn<>("Grade");
        gradeColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getGrade()));
        gradeColumn.setPrefWidth(70);

        TableColumn<Result, String> outcomeColumn = new TableColumn<>("Result");
        outcomeColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().isPassed() ? "PASS" : "FAIL"));
        outcomeColumn.setPrefWidth(75);

        TableColumn<Result, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getAttemptDate()));
        dateColumn.setPrefWidth(160);

        resultsTable.getColumns().addAll(idColumn, studentColumn, quizColumn, scoreColumn,
                percentColumn, gradeColumn, outcomeColumn, dateColumn);
    }

    private void buildStudentsTable() {
        styleTable(studentsTable);
        studentsTable.setPlaceholder(UiFactory.emptyState("No student attempts yet."));

        TableColumn<StudentSummary, String> nameColumn = new TableColumn<>("Student");
        nameColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getName()));
        nameColumn.setPrefWidth(230);

        TableColumn<StudentSummary, String> attemptsColumn = new TableColumn<>("Attempts");
        attemptsColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(String.valueOf(data.getValue().getAttempts())));
        attemptsColumn.setPrefWidth(100);

        TableColumn<StudentSummary, String> averageColumn = new TableColumn<>("Average");
        averageColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                String.format("%.1f%%", data.getValue().getAverage())));
        averageColumn.setPrefWidth(110);

        TableColumn<StudentSummary, String> bestColumn = new TableColumn<>("Best");
        bestColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                String.format("%.1f%%", data.getValue().getBest())));
        bestColumn.setPrefWidth(110);

        TableColumn<StudentSummary, String> passedColumn = new TableColumn<>("Passed");
        passedColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getPassed() + " / " + data.getValue().getAttempts()));
        passedColumn.setPrefWidth(110);

        studentsTable.getColumns().addAll(nameColumn, attemptsColumn, averageColumn,
                bestColumn, passedColumn);
    }

    private void buildQuizzesTable() {
        styleTable(quizzesTable);
        quizzesTable.setPlaceholder(UiFactory.emptyState("No quiz attempts yet."));

        TableColumn<QuizSummary, String> titleColumn = new TableColumn<>("Quiz");
        titleColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getTitle()));
        titleColumn.setPrefWidth(240);

        TableColumn<QuizSummary, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getCategory()));
        categoryColumn.setPrefWidth(150);

        TableColumn<QuizSummary, String> attemptsColumn = new TableColumn<>("Attempts");
        attemptsColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(String.valueOf(data.getValue().getAttempts())));
        attemptsColumn.setPrefWidth(100);

        TableColumn<QuizSummary, String> averageColumn = new TableColumn<>("Average");
        averageColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                String.format("%.1f%%", data.getValue().getAverage())));
        averageColumn.setPrefWidth(110);

        TableColumn<QuizSummary, String> bestColumn = new TableColumn<>("Best");
        bestColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                String.format("%.1f%%", data.getValue().getBest())));
        bestColumn.setPrefWidth(110);

        quizzesTable.getColumns().addAll(titleColumn, categoryColumn, attemptsColumn,
                averageColumn, bestColumn);
    }

    private void styleTable(TableView<?> table) {
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMinWidth(0);
    }

    public Button getRefreshButton() {
        return refreshButton;
    }

    public void setError(String text) {
        UiFactory.error(message, text);
    }

    public void clearMessage() {
        UiFactory.clear(message);
    }

    public void selectTab(int index) {
        if (index >= 0 && index < tabs.getTabs().size()) {
            tabs.getSelectionModel().select(index);
        }
    }

    public void setStats(int attempts, double average, double highest, int passed, int failed) {
        attemptsCard.setValue(String.valueOf(attempts));
        attemptsCard.setDescription(attempts == 0 ? "No attempts yet"
                : attempts + (attempts == 1 ? " graded result" : " graded results"));
        averageCard.setValue(String.format("%.1f%%", average));
        highestCard.setValue(String.format("%.1f%%", highest));
        int rate = attempts == 0 ? 0 : (int) Math.round(passed * 100.0 / attempts);
        passRateCard.setValue(rate + "%");
        passRateCard.setDescription(attempts == 0 ? "No attempts yet"
                : passed + " of " + attempts + " passed");
        passBar.setProgress(attempts == 0 ? 0.0 : (double) passed / attempts);
        passLabel.setText("Passed: " + passed);
        failLabel.setText("Not passed: " + failed);
        passRateLabel.setText(rate + "% pass rate");
        if (attempts == 0) {
            passRateLabel.setText("No attempts yet");
        }
    }

    public void setResults(List<Result> results, Map<Integer, String> studentNames,
                           Map<Integer, String> quizTitles) {
        this.studentNames = studentNames == null ? Collections.emptyMap() : studentNames;
        this.quizTitles = quizTitles == null ? Collections.emptyMap() : quizTitles;
        resultsTable.setItems(FXCollections.observableArrayList(results));
        resultsTable.refresh();
    }

    public void setStudentSummaries(List<StudentSummary> summaries) {
        studentsTable.setItems(FXCollections.observableArrayList(summaries));
    }

    public void setQuizSummaries(List<QuizSummary> summaries) {
        quizzesTable.setItems(FXCollections.observableArrayList(summaries));
    }
}
