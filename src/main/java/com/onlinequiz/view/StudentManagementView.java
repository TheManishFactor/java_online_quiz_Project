package com.onlinequiz.view;

import com.onlinequiz.model.User;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Administrator view of registered students, searchable by name or email, with
 * performance details and no access to password hashes.
 */
public final class StudentManagementView extends VBox {

    private final TextField searchField = UiFactory.textField("Search by name or email");
    private final TableView<User> table = new TableView<>();
    private final TableView<HistoryView.HistoryRow> resultsTable = new TableView<>();
    private final Label listCount = new Label();
    private final Label avatar = new Label("?");
    private final Label nameLabel = new Label("Select a student");
    private final Label emailLabel = new Label("");
    private final Label idLabel = new Label("");
    private final StatCard attemptsCard = new StatCard("◷", "Attempts", "-",
            "Graded results", "accent-indigo");
    private final StatCard averageCard = new StatCard("◔", "Average", "-",
            "Mean percentage", "accent-violet");
    private final StatCard bestCard = new StatCard("★", "Best", "-",
            "Highest score", "accent-sky");
    private final StatCard passCard = new StatCard("✓", "Passed", "-",
            "Passing attempts", "accent-green");
    private final Label message = UiFactory.messageLabel();
    private final Button refreshButton = UiFactory.ghostButton("Refresh");

    public StudentManagementView() {
        setSpacing(16);
        setPadding(new Insets(2));
        setMinWidth(0);

        HBox header = new HBox(16,
                UiFactory.pageHeader("PEOPLE", "Student management",
                        "Search registered students and inspect their quiz performance."),
                UiFactory.spacer(), refreshButton);
        header.setAlignment(Pos.CENTER_LEFT);

        searchField.setMaxWidth(340);
        listCount.getStyleClass().add("muted-text");

        buildTable();
        HBox listHeader = new HBox(12,
                UiFactory.sectionHeader("Registered students",
                        "Select a student to inspect their results."),
                UiFactory.spacer(), listCount);
        listHeader.setAlignment(Pos.CENTER_LEFT);

        VBox tableCard = UiFactory.card(listHeader, searchField, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        avatar.getStyleClass().addAll("avatar", "avatar-lg");
        nameLabel.getStyleClass().add("detail-name");
        emailLabel.getStyleClass().add("muted-text");
        idLabel.getStyleClass().add("muted-text");

        VBox profileText = new VBox(3, nameLabel, emailLabel, idLabel);
        profileText.setMinWidth(0);
        HBox.setHgrow(profileText, Priority.ALWAYS);
        HBox profileRow = new HBox(12, avatar, profileText);
        profileRow.setAlignment(Pos.CENTER_LEFT);

        GridPane statsTop = UiFactory.columns(10, attemptsCard, averageCard);
        GridPane statsBottom = UiFactory.columns(10, bestCard, passCard);

        buildResultsTable();

        VBox details = UiFactory.card(
                profileRow,
                UiFactory.separator(),
                statsTop,
                statsBottom,
                UiFactory.sectionHeader("Attempts", "Results for the selected student"),
                resultsTable,
                message);
        details.setPrefWidth(390);
        VBox.setVgrow(resultsTable, Priority.ALWAYS);

        GridPane body = UiFactory.split(tableCard, details, 60);
        VBox.setVgrow(body, Priority.ALWAYS);

        getChildren().addAll(header, body);
    }

    private void buildTable() {
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(UiFactory.emptyState("No student accounts found."));
        table.setMinWidth(0);

        TableColumn<User, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(String.valueOf(data.getValue().getId())));
        idColumn.setPrefWidth(55);

        TableColumn<User, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getName()));
        nameColumn.setPrefWidth(190);

        TableColumn<User, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getEmail()));
        emailColumn.setPrefWidth(260);

        table.getColumns().addAll(idColumn, nameColumn, emailColumn);
    }

    private void buildResultsTable() {
        resultsTable.getStyleClass().add("data-table");
        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        resultsTable.setPlaceholder(UiFactory.emptyState("No attempts."));
        resultsTable.setMinWidth(0);
        resultsTable.setPrefHeight(210);

        TableColumn<HistoryView.HistoryRow, String> quizColumn = new TableColumn<>("Quiz");
        quizColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getQuizTitle()));
        quizColumn.setPrefWidth(150);

        TableColumn<HistoryView.HistoryRow, String> scoreColumn = new TableColumn<>("Score");
        scoreColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getScore()));
        scoreColumn.setPrefWidth(70);

        TableColumn<HistoryView.HistoryRow, String> percentColumn = new TableColumn<>("%");
        percentColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getPercentage()));
        percentColumn.setPrefWidth(65);

        resultsTable.getColumns().addAll(quizColumn, scoreColumn, percentColumn);
    }

    public TextField getSearchField() {
        return searchField;
    }

    public TableView<User> getTable() {
        return table;
    }

    public Button getRefreshButton() {
        return refreshButton;
    }

    public void setTableItems(ObservableList<User> items) {
        table.setItems(items);
        int count = items == null ? 0 : items.size();
        listCount.setText(count == 1 ? "1 student" : count + " students");
    }

    public void showDetails(User student, List<HistoryView.HistoryRow> rows,
                            int attempts, double average, double best, int passed) {
        if (student == null) {
            clearDetails();
            return;
        }
        nameLabel.setText(student.getName());
        emailLabel.setText(student.getEmail());
        idLabel.setText("Student ID " + student.getId());
        avatar.setText(student.getName() == null || student.getName().isBlank()
                ? "?" : student.getName().substring(0, 1).toUpperCase());
        attemptsCard.setValue(String.valueOf(attempts));
        averageCard.setValue(String.format("%.1f%%", average));
        bestCard.setValue(String.format("%.1f%%", best));
        passCard.setValue(passed + " / " + attempts);
        resultsTable.setItems(javafx.collections.FXCollections.observableArrayList(rows));
        UiFactory.clear(message);
    }

    public void clearDetails() {
        nameLabel.setText("Select a student");
        emailLabel.setText("");
        idLabel.setText("");
        avatar.setText("?");
        attemptsCard.setValue("-");
        averageCard.setValue("-");
        bestCard.setValue("-");
        passCard.setValue("-");
        resultsTable.setItems(javafx.collections.FXCollections.observableArrayList());
    }

    public void setMessage(String text, boolean error) {
        if (error) {
            UiFactory.error(message, text);
        } else {
            UiFactory.success(message, text);
        }
    }
}
