package com.onlinequiz.controller;

import com.onlinequiz.exception.QuizNotFoundException;
import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.model.Question;
import com.onlinequiz.model.Quiz;
import com.onlinequiz.navigation.NavigationManager;
import com.onlinequiz.navigation.ScreenLifecycle;
import com.onlinequiz.service.QuestionService;
import com.onlinequiz.service.QuizService;
import com.onlinequiz.threads.BackgroundTask;
import com.onlinequiz.util.ExceptionHandler;
import com.onlinequiz.view.QuestionManagementView;
import com.onlinequiz.view.UiFactory;
import javafx.application.Platform;

import java.util.ArrayList;

/**
 * Administrator question authoring. Questions are always loaded for the quiz
 * currently selected in the combo box, and every mutation reloads from SQLite so
 * question counts stay correct.
 */
public final class QuestionManagementController implements ScreenLifecycle {

    private final NavigationManager navigation;
    private final QuizService quizService;
    private final QuestionService questionService;
    private final QuestionManagementView view;

    private volatile boolean active = true;
    private boolean updatingSelector;

    public QuestionManagementController(NavigationManager navigation, QuizService quizService,
                                        QuestionService questionService,
                                        QuestionManagementView view) {
        this.navigation = navigation;
        this.quizService = quizService;
        this.questionService = questionService;
        this.view = view;

        view.getQuizSelector().valueProperty().addListener((observable, previous, quiz) -> {
            if (!updatingSelector) {
                loadQuestions(null);
            }
        });
        view.getTable().getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, question) -> {
                    if (question != null) {
                        view.showQuestion(question);
                    }
                });
        view.getAddButton().setOnAction(event -> addQuestion());
        view.getSaveButton().setOnAction(event -> updateQuestion());
        view.getDeleteButton().setOnAction(event -> deleteQuestion());
        view.getClearButton().setOnAction(event -> view.clearForm());
        view.getRefreshButton().setOnAction(event -> reloadQuizzes(null));

        reloadQuizzes(null);
    }

    @Override
    public void onHide() {
        active = false;
    }

    private void reloadQuizzes(String successMessage) {
        Quiz current = view.getSelectedQuiz();
        Integer selectedId = current == null ? null : current.getId();
        view.setBusy(true);
        new BackgroundTask<>(quizService::getAllQuizzes, Platform::runLater)
                .onSuccess(quizzes -> {
                    if (!active) {
                        return;
                    }
                    updatingSelector = true;
                    view.setQuizzes(quizzes, selectedId);
                    updatingSelector = false;
                    view.setBusy(false);
                    view.clearForm();
                    loadQuestions(successMessage);
                })
                .onFailure(error -> {
                    if (!active) {
                        return;
                    }
                    view.setBusy(false);
                    view.setMessage("Could not load quizzes: " + describe(error), true);
                })
                .start();
    }

    private void loadQuestions(String successMessage) {
        Quiz quiz = view.getSelectedQuiz();
        if (quiz == null) {
            view.setQuestions(new ArrayList<>());
            if (successMessage != null) {
                view.setMessage(successMessage, false);
            }
            return;
        }
        view.setBusy(true);
        new BackgroundTask<>(() -> questionService.getQuestionsByQuizId(quiz.getId()),
                Platform::runLater)
                .onSuccess(questions -> {
                    if (!active) {
                        return;
                    }
                    view.setQuestions(questions);
                    view.setBusy(false);
                    if (successMessage != null) {
                        view.setMessage(successMessage, false);
                    }
                })
                .onFailure(error -> {
                    if (!active) {
                        return;
                    }
                    view.setBusy(false);
                    view.setMessage("Could not load questions: " + describe(error), true);
                })
                .start();
    }

    private void addQuestion() {
        Quiz quiz = view.getSelectedQuiz();
        if (quiz == null) {
            view.setMessage("Select a quiz before adding questions.", true);
            return;
        }
        try {
            Question question = new Question(quiz.getId(),
                    view.getQuestionText().getText().trim(),
                    view.getOptionA().getText().trim(),
                    view.getOptionB().getText().trim(),
                    view.getOptionC().getText().trim(),
                    view.getOptionD().getText().trim(),
                    correctAnswer());
            questionService.addQuestion(question);
            reloadQuizzes("Question added to '" + quiz.getTitle() + "'.");
        } catch (QuizNotFoundException | ValidationException error) {
            view.setMessage(describe(error), true);
        } catch (RuntimeException error) {
            ExceptionHandler.handle(error, "Question management");
            view.setMessage("Could not add the question. Please try again.", true);
        }
    }

    private void updateQuestion() {
        Question selected = view.getSelectedQuestion();
        if (selected == null) {
            view.setMessage("Select a question in the table first.", true);
            return;
        }
        Quiz quiz = view.getSelectedQuiz();
        if (quiz == null) {
            view.setMessage("Select a quiz first.", true);
            return;
        }
        try {
            Question updated = new Question(selected.getId(), quiz.getId(),
                    view.getQuestionText().getText().trim(),
                    view.getOptionA().getText().trim(),
                    view.getOptionB().getText().trim(),
                    view.getOptionC().getText().trim(),
                    view.getOptionD().getText().trim(),
                    correctAnswer());
            if (!questionService.updateQuestion(updated)) {
                view.setMessage("Question " + selected.getId() + " no longer exists.", true);
                return;
            }
            reloadQuizzes("Question updated.");
        } catch (ValidationException error) {
            view.setMessage(describe(error), true);
        } catch (RuntimeException error) {
            ExceptionHandler.handle(error, "Question management");
            view.setMessage("Could not update the question. Please try again.", true);
        }
    }

    private void deleteQuestion() {
        Question selected = view.getSelectedQuestion();
        if (selected == null) {
            view.setMessage("Select a question in the table first.", true);
            return;
        }
        boolean confirmed = UiFactory.confirm(
                navigation.getStage(), "Delete question",
                "Delete question " + selected.getId() + "?",
                "This question will be removed from the quiz. This cannot be undone.");
        if (!confirmed) {
            return;
        }
        try {
            if (questionService.deleteQuestion(selected.getId())) {
                reloadQuizzes("Question deleted.");
            } else {
                view.setMessage("Question " + selected.getId() + " no longer exists.", true);
            }
        } catch (RuntimeException error) {
            view.setMessage("Could not delete the question: " + describe(error), true);
        }
    }

    private String correctAnswer() {
        String value = view.getCorrectAnswer().getValue();
        return value == null ? "A" : value;
    }

    private static String describe(Throwable error) {
        String message = error.getMessage();
        return message == null || message.trim().isEmpty()
                ? error.getClass().getSimpleName()
                : message;
    }
}
