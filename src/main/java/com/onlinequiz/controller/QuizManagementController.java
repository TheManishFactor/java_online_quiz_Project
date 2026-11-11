package com.onlinequiz.controller;

import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.model.Quiz;
import com.onlinequiz.navigation.NavigationManager;
import com.onlinequiz.navigation.ScreenLifecycle;
import com.onlinequiz.service.QuizService;
import com.onlinequiz.threads.BackgroundTask;
import com.onlinequiz.util.ExceptionHandler;
import com.onlinequiz.view.QuizManagementView;
import com.onlinequiz.view.UiFactory;
import javafx.application.Platform;

/**
 * Administrator quiz CRUD. Every write goes through {@link QuizService}; the view
 * refreshes from SQLite afterwards so it never shows optimistic local state.
 */
public final class QuizManagementController implements ScreenLifecycle {

    private final NavigationManager navigation;
    private final QuizService quizService;
    private final QuizManagementView view;

    private volatile boolean active = true;

    public QuizManagementController(NavigationManager navigation, QuizService quizService,
                                    QuizManagementView view) {
        this.navigation = navigation;
        this.quizService = quizService;
        this.view = view;

        view.getTable().getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, quiz) -> {
                    if (quiz != null) {
                        view.showQuiz(quiz);
                    }
                });
        view.getCreateButton().setOnAction(event -> createQuiz());
        view.getSaveButton().setOnAction(event -> updateQuiz());
        view.getDeleteButton().setOnAction(event -> deleteQuiz());
        view.getClearButton().setOnAction(event -> view.clearForm());
        view.getRefreshButton().setOnAction(event -> load(null));

        load(null);
    }

    @Override
    public void onHide() {
        active = false;
    }

    private void load(String successMessage) {
        view.setBusy(true);
        new BackgroundTask<>(quizService::getAllQuizzes, Platform::runLater)
                .onSuccess(quizzes -> {
                    if (!active) {
                        return;
                    }
                    view.setQuizzes(quizzes);
                    view.setBusy(false);
                    view.clearForm();
                    if (successMessage != null) {
                        view.setMessage(successMessage, false);
                    }
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

    private void createQuiz() {
        try {
            Quiz quiz = new Quiz(view.getTitleField().getText().trim(),
                    view.getCategoryField().getText().trim(),
                    parseDuration());
            quizService.addQuiz(quiz);
            load("Quiz '" + quiz.getTitle() + "' created.");
        } catch (ValidationException error) {
            view.setMessage(describe(error), true);
        } catch (RuntimeException error) {
            ExceptionHandler.handle(error, "Quiz management");
            view.setMessage("Could not create the quiz. Please try again.", true);
        }
    }

    private void updateQuiz() {
        Quiz selected = view.getSelectedQuiz();
        if (selected == null) {
            view.setMessage("Select a quiz in the table first.", true);
            return;
        }
        try {
            Quiz updated = new Quiz(selected.getId(),
                    view.getTitleField().getText().trim(),
                    view.getCategoryField().getText().trim(),
                    parseDuration(),
                    selected.getTotalQuestions());
            if (!quizService.updateQuiz(updated)) {
                view.setMessage("Quiz " + selected.getId() + " no longer exists.", true);
                return;
            }
            load("Quiz '" + updated.getTitle() + "' updated.");
        } catch (ValidationException error) {
            view.setMessage(describe(error), true);
        } catch (RuntimeException error) {
            ExceptionHandler.handle(error, "Quiz management");
            view.setMessage("Could not update the quiz. Please try again.", true);
        }
    }

    private void deleteQuiz() {
        Quiz selected = view.getSelectedQuiz();
        if (selected == null) {
            view.setMessage("Select a quiz in the table first.", true);
            return;
        }
        boolean confirmed = UiFactory.confirm(navigation.getStage(), "Delete quiz",
                "Delete '" + selected.getTitle() + "'?",
                "Its questions and every attempt on it will be removed. This cannot be undone.");
        if (!confirmed) {
            return;
        }
        try {
            if (quizService.deleteQuiz(selected.getId())) {
                load("Quiz '" + selected.getTitle() + "' deleted.");
            } else {
                view.setMessage("Quiz " + selected.getId() + " no longer exists.", true);
            }
        } catch (RuntimeException error) {
            view.setMessage("Could not delete the quiz: " + describe(error), true);
        }
    }

    private int parseDuration() {
        String text = view.getDurationField().getText();
        try {
            return Integer.parseInt(text == null ? "" : text.trim());
        } catch (NumberFormatException error) {
            throw new ValidationException("Duration must be a whole number of minutes");
        }
    }

    private static String describe(Throwable error) {
        String message = error.getMessage();
        return message == null || message.trim().isEmpty()
                ? error.getClass().getSimpleName()
                : message;
    }
}
