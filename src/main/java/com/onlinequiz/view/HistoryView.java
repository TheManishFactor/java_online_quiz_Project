package com.onlinequiz.view;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Student attempt history with performance summary and text export.
 */
public final class HistoryView extends VBox {

    /**
     * Display row for the history table.
     */
    public static final class HistoryRow {

        private final String quizTitle;
        private final int score;
        private final int total;
        private final double percentage;
        private final String grade;
        private final String date;
        private final boolean passed;

        public HistoryRow(String quizTitle, int score, int total, double percentage,
                          String grade, String date, boolean passed) {
            this.quizTitle = quizTitle;
            this.score = score;
            this.total = total;
            this.percentage = percentage;
            this.grade = grade;
            this.date = date;
            this.passed = passed;
        }

        public String getQuizTitle() {
            return quizTitle;
        }

        public String getScore() {
            return score + " / " + total;
        }

        public String getPercentage() {
            return percentage + "%";
        }

        public String getGrade() {
            return grade;
        }

        public String getDate() {
            return date;
        }

        public String getOutcome() {
            return passed ? "PASS" : "FAIL";
        }
    }

    private final StatCard attemptsCard = new StatCard("◷", "Attempts", "-",
            "Recorded attempts", "accent-indigo");
    private final StatCard averageCard = new StatCard("◔", "Average", "-",
            "Mean percentage", "accent-violet");
    private final StatCard bestCard = new StatCard("★", "Best score", "-",
            "Highest percentage", "accent-sky");
    private final StatCard passedCard = new StatCard("✓", "Passed", "-",
            "Passing attempts", "accent-green");
    private final TableView<HistoryRow> table = new TableView<>();
    private final Button exportButton = UiFactory.primaryButton("Export results");
    private final Button refreshButton = UiFactory.ghostButton("Refresh");
    private final Label message = UiFactory.messageLabel();

    public HistoryView() {
        setSpacing(16);
        setPadding(new Insets(2));

        HBox header = new HBox(16,
                UiFactory.pageHeader("PERFORMANCE", "My results",
                        "Every attempt is stored in SQLite. Export a plain-text report at any time."),
                UiFactory.spacer(), refreshButton, exportButton);
        header.setAlignment(Pos.CENTER_LEFT);

        HBox stats = new HBox(14, attemptsCard, averageCard, bestCard, passedCard);
        for (javafx.scene.Node card : stats.getChildren()) {
            HBox.setHgrow(card, Priority.ALWAYS);
            ((StatCard) card).setMaxWidth(Double.MAX_VALUE);
        }

        buildTable();
        HBox tableHeader = new HBox(12,
                UiFactory.sectionHeader("Attempt history", "Newest attempts appear first."),
                UiFactory.spacer(), UiFactory.muted("All data from your account"));
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        VBox tableCard = UiFactory.card(tableHeader, table);
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setPrefHeight(360);

        getChildren().addAll(header, stats, message, tableCard);
    }

    private void buildTable() {
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(UiFactory.emptyState(
                "No attempts yet. Finish a quiz and your results will appear here."));

        TableColumn<HistoryRow, String> quizColumn = new TableColumn<>("Quiz");
        quizColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getQuizTitle()));
        quizColumn.setPrefWidth(270);

        TableColumn<HistoryRow, String> scoreColumn = new TableColumn<>("Score");
        scoreColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getScore()));
        scoreColumn.setPrefWidth(110);

        TableColumn<HistoryRow, String> percentageColumn = new TableColumn<>("Percentage");
        percentageColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getPercentage()));
        percentageColumn.setPrefWidth(120);

        TableColumn<HistoryRow, String> gradeColumn = new TableColumn<>("Grade");
        gradeColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getGrade()));
        gradeColumn.setPrefWidth(90);

        TableColumn<HistoryRow, String> outcomeColumn = new TableColumn<>("Result");
        outcomeColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getOutcome()));
        outcomeColumn.setPrefWidth(100);
        outcomeColumn.setCellFactory(column -> new TableCell<HistoryRow, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(null);
                setGraphic(UiFactory.badge(value,
                        "PASS".equals(value) ? "badge-pass" : "badge-fail"));
            }
        });

        TableColumn<HistoryRow, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getDate()));
        dateColumn.setPrefWidth(190);

        table.getColumns().addAll(quizColumn, scoreColumn, percentageColumn,
                gradeColumn, outcomeColumn, dateColumn);
    }

    public Button getExportButton() {
        return exportButton;
    }

    public Button getRefreshButton() {
        return refreshButton;
    }

    public void setSummary(int attempts, double average, double best, int passed) {
        attemptsCard.setValue(String.valueOf(attempts));
        averageCard.setValue(String.format("%.1f%%", average));
        bestCard.setValue(String.format("%.1f%%", best));
        passedCard.setValue(passed + " / " + attempts);
        attemptsCard.setDescription(attempts == 1 ? "1 recorded attempt" : attempts + " recorded attempts");
        passedCard.setDescription(attempts == 0 ? "No attempts yet"
                : String.format("%.0f%% pass rate", (passed * 100.0) / attempts));
    }

    public void setRows(List<HistoryRow> rows) {
        table.setItems(FXCollections.observableArrayList(rows));
    }

    public void setBusy(boolean busy) {
        exportButton.setDisable(busy);
        refreshButton.setDisable(busy);
        table.setDisable(busy);
    }

    public void setMessage(String text, boolean error) {
        if (error) {
            UiFactory.error(message, text);
        } else {
            UiFactory.success(message, text);
        }
    }

    public void clearMessage() {
        UiFactory.clear(message);
    }
}
