package com.onlinequiz.controller;

import com.onlinequiz.generics.GenericRepository;
import com.onlinequiz.generics.GenericUtils;
import com.onlinequiz.model.Quiz;
import com.onlinequiz.navigation.NavigationManager;
import com.onlinequiz.navigation.ScreenLifecycle;
import com.onlinequiz.service.QuizService;
import com.onlinequiz.threads.BackgroundTask;
import com.onlinequiz.view.QuizSelectionView;
import javafx.application.Platform;

import java.util.List;

/**
 * Loads the quiz catalog in the background and caches it in a generic repository.
 * The cache is also a practical use of the Phase 5 generics layer.
 */
public final class QuizSelectionController implements ScreenLifecycle {

    private final NavigationManager navigation;
    private final QuizService quizService;
    private final QuizSelectionView view;
    private final GenericRepository<Quiz> cache = new GenericRepository<>();

    private volatile boolean active = true;

    public QuizSelectionController(NavigationManager navigation, QuizService quizService,
                                   QuizSelectionView view) {
        this.navigation = navigation;
        this.quizService = quizService;
        this.view = view;

        view.getRefreshButton().setOnAction(event -> load());
        load();
    }

    @Override
    public void onHide() {
        active = false;
    }

    private void load() {
        view.showLoading();
        new BackgroundTask<>(quizService::getAllQuizzes, Platform::runLater)
                .onSuccess(quizzes -> {
                    if (!active) {
                        return;
                    }
                    cache.clear();
                    for (Quiz quiz : quizzes) {
                        cache.add(quiz);
                    }
                    view.showQuizzes(quizzes, this::startQuiz);
                })
                .onFailure(error -> {
                    if (active) {
                        view.showError("Could not load quizzes. Please refresh.");
                    }
                })
                .start();
    }

    private void startQuiz(Quiz selected) {
        Quiz cached = GenericUtils.find(cache.getAll(),
                quiz -> quiz.getId() == selected.getId());
        Quiz quiz = cached == null ? selected : cached;
        if (!quiz.hasQuestions()) {
            navigation.showError("Quiz unavailable",
                    "This quiz has no questions yet. An administrator must add them first.");
            return;
        }
        navigation.showQuizAttempt(quiz);
    }
}
