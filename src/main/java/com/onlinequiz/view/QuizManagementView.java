package com.onlinequiz.view;

import com.onlinequiz.model.Quiz;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
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
 * Quiz CRUD screen: table on the left, editor on the right.
 */
public final class QuizManagementView extends VBox {

    private final TableView<Quiz> table = new TableView<>();
    private final Label tableCount = new Label();
    private final TextField titleField = UiFactory.textField("Quiz title");
    private final TextField categoryField = UiFactory.textField("Category");
    private final TextField durationField = UiFactory.textField("Duration in minutes");
    private final Button createButton = UiFactory.primaryButton("Create quiz");
    private final Button saveButton = UiFactory.secondaryButton("Save changes");
    private final Button deleteButton = UiFactory.dangerButton("Delete quiz");
    private final Button clearButton = UiFactory.ghostButton("Clear form");
    private final Button refreshButton = UiFactory.ghostButton("Refresh");
    private final Label message = UiFactory.messageLabel();

    public QuizManagementView() {
        setSpacing(16);
        setPadding(new Insets(2));
        setMinWidth(0);

        HBox header = new HBox(16,
                UiFactory.pageHeader("CONTENT LIBRARY", "Quiz management",
                        "Create, edit and delete quizzes. Deleting a quiz also removes its"
                                + " questions and attempts."),
                UiFactory.spacer(), refreshButton);
        header.setAlignment(Pos.CENTER_LEFT);

        buildTable();
        tableCount.getStyleClass().add("muted-text");
        HBox tableHeader = new HBox(12,
                UiFactory.sectionHeader("All quizzes", "Select a row to load it into the editor."),
                UiFactory.spacer(), tableCount);
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        VBox tableCard = UiFactory.card(tableHeader, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox form = UiFactory.card(
                UiFactory.sectionHeader("Quiz editor",
                        "Create a new quiz or update the selected one."),
                UiFactory.separator(),
                UiFactory.field("Title", titleField),
                UiFactory.field("Category", categoryField),
                UiFactory.field("Duration (minutes)", durationField),
                createButton,
                new HBox(8, saveButton, deleteButton),
                clearButton,
                message);
        form.setPrefWidth(330);

        GridPane body = UiFactory.split(tableCard, form, 66);
        VBox.setVgrow(body, Priority.ALWAYS);

        getChildren().addAll(header, body);
    }

    private void buildTable() {
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(UiFactory.emptyState("No quizzes yet. Create the first one."));
        table.setMinWidth(0);

        TableColumn<Quiz, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(String.valueOf(data.getValue().getId())));
        idColumn.setPrefWidth(60);

        TableColumn<Quiz, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getTitle()));
        titleColumn.setPrefWidth(220);

        TableColumn<Quiz, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getCategory()));
        categoryColumn.setPrefWidth(150);

        TableColumn<Quiz, String> questionsColumn = new TableColumn<>("Questions");
        questionsColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(String.valueOf(data.getValue().getTotalQuestions())));
        questionsColumn.setPrefWidth(100);

        TableColumn<Quiz, String> durationColumn = new TableColumn<>("Minutes");
        durationColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(String.valueOf(data.getValue().getDuration())));
        durationColumn.setPrefWidth(90);

        table.getColumns().addAll(idColumn, titleColumn, categoryColumn,
                questionsColumn, durationColumn);
    }

    public TableView<Quiz> getTable() {
        return table;
    }

    public TextField getTitleField() {
        return titleField;
    }

    public TextField getCategoryField() {
        return categoryField;
    }

    public TextField getDurationField() {
        return durationField;
    }

    public Button getCreateButton() {
        return createButton;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    public Button getClearButton() {
        return clearButton;
    }

    public Button getRefreshButton() {
        return refreshButton;
    }

    public Quiz getSelectedQuiz() {
        return table.getSelectionModel().getSelectedItem();
    }

    public void setQuizzes(List<Quiz> quizzes) {
        table.setItems(FXCollections.observableArrayList(quizzes));
        int count = quizzes == null ? 0 : quizzes.size();
        tableCount.setText(count == 1 ? "1 quiz" : count + " quizzes");
    }

    public void showQuiz(Quiz quiz) {
        if (quiz == null) {
            clearForm();
            return;
        }
        titleField.setText(quiz.getTitle());
        categoryField.setText(quiz.getCategory());
        durationField.setText(String.valueOf(quiz.getDuration()));
    }

    public void clearForm() {
        table.getSelectionModel().clearSelection();
        titleField.clear();
        categoryField.clear();
        durationField.clear();
        UiFactory.clear(message);
    }

    public void setBusy(boolean busy) {
        createButton.setDisable(busy);
        saveButton.setDisable(busy);
        deleteButton.setDisable(busy);
        clearButton.setDisable(busy);
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
}
