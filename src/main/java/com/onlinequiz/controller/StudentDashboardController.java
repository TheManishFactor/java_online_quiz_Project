package com.onlinequiz.controller;

import com.onlinequiz.model.Quiz;
import com.onlinequiz.model.Result;
import com.onlinequiz.model.User;
import com.onlinequiz.navigation.NavigationManager;
import com.onlinequiz.navigation.ScreenLifecycle;
import com.onlinequiz.service.QuizService;
import com.onlinequiz.service.ResultService;
import com.onlinequiz.threads.BackgroundTask;
import com.onlinequiz.view.StudentDashboardView;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads the student overview through the service layer on a background thread so
 * the JavaFX Application Thread stays responsive.
 */
public final class StudentDashboardController implements ScreenLifecycle {

    private final NavigationManager navigation;
    private final QuizService quizService;
    private final ResultService resultService;
    private final User student;
    private final StudentDashboardView view;

    private volatile boolean active = true;

    public StudentDashboardController(NavigationManager navigation, QuizService quizService,
                                      ResultService resultService, User student,
                                      StudentDashboardView view) {
        this.navigation = navigation;
        this.quizService = quizService;
        this.resultService = resultService;
        this.student = student;
        this.view = view;

        view.setWelcome(student.getName());
        view.getStartQuizButton().setOnAction(event -> navigation.showQuizSelection());
        view.getQuizzesButton().setOnAction(event -> navigation.showQuizSelection());
        view.getResultsButton().setOnAction(event -> navigation.showHistory());
        view.getProfileButton().setOnAction(event -> navigation.showProfileDialog());
        view.getLogoutButton().setOnAction(event -> navigation.logout());

        load();
    }

    @Override
    public void onHide() {
        active = false;
    }

    private void load() {
        new BackgroundTask<>(this::loadData, Platform::runLater)
                .onSuccess(data -> {
                    if (active) {
                        render(data);
                    }
                })
                .onFailure(error -> {
                    if (active) {
                        view.setStats(0, 0, 0.0, 0);
                        navigation.showError("Dashboard", "Could not load your dashboard data.");
                    }
                })
                .start();
    }

    private DashboardData loadData() {
        DashboardData data = new DashboardData();
        data.quizzes = quizService.getAllQuizzes();
        data.categories = quizService.getCategories();
        data.results = new ArrayList<>(resultService.getResultsByStudentId(student.getId()));
        data.results.sort(Comparator.comparingInt(Result::getId).reversed());

        for (Quiz quiz : data.quizzes) {
            data.quizTitles.put(quiz.getId(), quiz.getTitle());
        }
        double total = 0.0;
        for (Result result : data.results) {
            total += result.getPercentage();
        }
        data.average = data.results.isEmpty() ? 0.0 : total / data.results.size();
        return data;
    }

    private void render(DashboardData data) {
        view.setStats(data.quizzes.size(), data.results.size(), data.average,
                data.categories.size());
        view.setCategories(data.categories);
        view.setRecentResults(data.results, data.quizTitles);
    }

    private static final class DashboardData {

        private List<Quiz> quizzes = new ArrayList<>();
        private List<Result> results = new ArrayList<>();
        private java.util.Set<String> categories = new java.util.TreeSet<>();
        private final Map<Integer, String> quizTitles = new HashMap<>();
        private double average;
    }
}
