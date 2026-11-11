package com.onlinequiz.view;

import com.onlinequiz.model.Question;
import com.onlinequiz.model.Quiz;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;

/**
 * Four-option question editor. A quiz must be selected before questions can be
 * listed or changed.
 */
public final class QuestionManagementView extends VBox {

    private final ComboBox<Quiz> quizSelector = new ComboBox<>();
    private final TableView<Question> table = new TableView<>();
    private final Label tableCount = new Label();
    private final TextArea questionText = new TextArea();
    private final TextField optionA = UiFactory.textField("Option A");
    private final TextField optionB = UiFactory.textField("Option B");
    private final TextField optionC = UiFactory.textField("Option C");
    private final TextField optionD = UiFactory.textField("Option D");
    private final ComboBox<String> correctAnswer = new ComboBox<>();
    private final Button addButton = UiFactory.primaryButton("Add question");
    private final Button saveButton = UiFactory.secondaryButton("Save changes");
    private final Button deleteButton = UiFactory.dangerButton("Delete question");
    private final Button clearButton = UiFactory.ghostButton("Clear form");
    private final Button refreshButton = UiFactory.ghostButton("Refresh");
    private final Label message = UiFactory.messageLabel();

    public QuestionManagementView() {
        setSpacing(16);
        setPadding(new Insets(2));
        setMinWidth(0);

        HBox header = new HBox(16,
                UiFactory.pageHeader("QUESTION BANK", "Question management",
                        "Select a quiz, then author four-option multiple-choice questions."),
                UiFactory.spacer(), refreshButton);
        header.setAlignment(Pos.CENTER_LEFT);

        quizSelector.setMaxWidth(Double.MAX_VALUE);
        quizSelector.setConverter(new StringConverter<Quiz>() {
            @Override
            public String toString(Quiz quiz) {
                return quiz == null ? "" : quiz.getTitle() + "  •  "
                        + quiz.getTotalQuestions() + " questions";
            }

            @Override
            public Quiz fromString(String text) {
                return null;
            }
        });
        Label quizLabel = new Label("Quiz");
        quizLabel.getStyleClass().add("field-label");
        HBox selectorRow = new HBox(12, quizLabel, quizSelector);
        selectorRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(quizSelector, Priority.ALWAYS);

        VBox selectorCard = UiFactory.card(
                UiFactory.sectionHeader("Choose a quiz",
                        "Questions are listed and edited per quiz."),
                selectorRow);

        buildTable();
        tableCount.getStyleClass().add("muted-text");
        HBox tableHeader = new HBox(12,
                UiFactory.sectionHeader("Questions", "Select a question to load it below."),
                UiFactory.spacer(), tableCount);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        VBox tableCard = UiFactory.card(tableHeader, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        questionText.setPromptText("Question text");
        questionText.setWrapText(true);
        questionText.setPrefRowCount(3);
        questionText.getStyleClass().add("app-field");

        correctAnswer.getItems().addAll("A", "B", "C", "D");
        correctAnswer.getSelectionModel().select("A");
        correctAnswer.setMaxWidth(Double.MAX_VALUE);

        VBox form = UiFactory.card(
                UiFactory.sectionHeader("Question editor",
                        "Write the prompt and four options, then pick the correct letter."),
                UiFactory.separator(),
                UiFactory.field("Question", questionText),
                UiFactory.field("Option A", optionA),
                UiFactory.field("Option B", optionB),
                UiFactory.field("Option C", optionC),
                UiFactory.field("Option D", optionD),
                UiFactory.field("Correct answer", correctAnswer),
                addButton,
                new HBox(8, saveButton, deleteButton),
                clearButton,
                message);
        form.setMaxWidth(Double.MAX_VALUE);
        ScrollPane formScroll = UiFactory.scroll(form);
        formScroll.setPrefWidth(350);
        VBox.setVgrow(formScroll, Priority.ALWAYS);

        GridPane body = UiFactory.split(tableCard, formScroll, 63);
        VBox.setVgrow(body, Priority.ALWAYS);

        getChildren().addAll(header, selectorCard, body);
    }

    private void buildTable() {
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(UiFactory.emptyState(
                "Select a quiz, then add its first question."));
        table.setMinWidth(0);

        TableColumn<Question, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(String.valueOf(data.getValue().getId())));
        idColumn.setPrefWidth(55);

        TableColumn<Question, String> textColumn = new TableColumn<>("Question");
        textColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getQuestionText()));
        textColumn.setPrefWidth(360);

        TableColumn<Question, String> answerColumn = new TableColumn<>("Answer");
        answerColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getCorrectAnswer()));
        answerColumn.setPrefWidth(75);

        table.getColumns().addAll(idColumn, textColumn, answerColumn);
    }

    public ComboBox<Quiz> getQuizSelector() {
        return quizSelector;
    }

    public TableView<Question> getTable() {
        return table;
    }

    public TextArea getQuestionText() {
        return questionText;
    }

    public TextField getOptionA() {
        return optionA;
    }

    public TextField getOptionB() {
        return optionB;
    }

    public TextField getOptionC() {
        return optionC;
    }

    public TextField getOptionD() {
        return optionD;
    }

    public ComboBox<String> getCorrectAnswer() {
        return correctAnswer;
    }

    public Button getAddButton() {
        return addButton;
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
        return quizSelector.getSelectionModel().getSelectedItem();
    }

    public Question getSelectedQuestion() {
        return table.getSelectionModel().getSelectedItem();
    }

    public void setQuizzes(List<Quiz> quizzes, Integer selectedId) {
        quizSelector.setItems(FXCollections.observableArrayList(quizzes));
        if (selectedId != null) {
            for (Quiz quiz : quizzes) {
                if (quiz.getId() == selectedId) {
                    quizSelector.getSelectionModel().select(quiz);
                    return;
                }
            }
        }
        if (!quizzes.isEmpty() && quizSelector.getSelectionModel().isEmpty()) {
            quizSelector.getSelectionModel().selectFirst();
        }
    }

    public void setQuestions(List<Question> questions) {
        table.setItems(FXCollections.observableArrayList(questions));
        int count = questions == null ? 0 : questions.size();
        tableCount.setText(count == 1 ? "1 question" : count + " questions");
    }

    public void showQuestion(Question question) {
        if (question == null) {
            clearForm();
            return;
        }
        questionText.setText(question.getQuestionText());
        optionA.setText(question.getOptionA());
        optionB.setText(question.getOptionB());
        optionC.setText(question.getOptionC());
        optionD.setText(question.getOptionD());
        correctAnswer.getSelectionModel().select(question.getCorrectAnswer());
    }

    public void clearForm() {
        table.getSelectionModel().clearSelection();
        questionText.clear();
        optionA.clear();
        optionB.clear();
        optionC.clear();
        optionD.clear();
        correctAnswer.getSelectionModel().select("A");
        UiFactory.clear(message);
    }

    public void setBusy(boolean busy) {
        addButton.setDisable(busy);
        saveButton.setDisable(busy);
        deleteButton.setDisable(busy);
        clearButton.setDisable(busy);
        refreshButton.setDisable(busy);
        table.setDisable(busy);
        quizSelector.setDisable(busy);
    }

    public void setMessage(String text, boolean error) {
        if (error) {
            UiFactory.error(message, text);
        } else {
            UiFactory.success(message, text);
        }
    }
}
