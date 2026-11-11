package com.onlinequiz.controller;

import com.onlinequiz.generics.GenericUtils;
import com.onlinequiz.model.Quiz;
import com.onlinequiz.model.Result;
import com.onlinequiz.model.User;
import com.onlinequiz.navigation.NavigationManager;
import com.onlinequiz.navigation.ScreenLifecycle;
import com.onlinequiz.service.QuizService;
import com.onlinequiz.service.ResultService;
import com.onlinequiz.service.UserService;
import com.onlinequiz.threads.BackgroundTask;
import com.onlinequiz.view.AdminDashboardView;
import javafx.application.Platform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Administrator overview. Statistics are collected on a background thread through
 * the existing services; no SQL is written in the controller.
 */
public final class AdminDashboardController implements ScreenLifecycle {

    private final NavigationManager navigation;
    private final UserService userService;
    private final QuizService quizService;
    private final ResultService resultService;
    private final User admin;
    private final AdminDashboardView view;

    private volatile boolean active = true;

    public AdminDashboardController(NavigationManager navigation, UserService userService,
                                    QuizService quizService, ResultService resultService,
                                    User admin, AdminDashboardView view) {
        this.navigation = navigation;
        this.userService = userService;
        this.quizService = quizService;
        this.resultService = resultService;
        this.admin = admin;
        this.view = view;

        view.setWelcome(admin.getName());
        view.getManageQuizzesButton().setOnAction(event -> navigation.showQuizManagement());
        view.getManageQuestionsButton().setOnAction(event -> navigation.showQuestionManagement());
        view.getManageStudentsButton().setOnAction(event -> navigation.showStudentManagement());
        view.getViewResultsButton().setOnAction(event -> navigation.showAnalytics(0));
        view.getAnalyticsButton().setOnAction(event -> navigation.showAnalytics(3));
        view.getRefreshButton().setOnAction(event -> load());
        view.getLogoutButton().setOnAction(event -> navigation.logout());

        load();
    }

    @Override
    public void onHide() {
        active = false;
    }

    private void load() {
        new BackgroundTask<>(this::loadStats, Platform::runLater)
                .onSuccess(stats -> {
                    if (active) {
                        view.setStats(stats.students, stats.quizzes, stats.questions,
                                stats.attempts, stats.average);
                        view.setRecentActivity(stats.results, stats.studentNames,
                                stats.quizTitles);
                    }
                })
                .onFailure(error -> {
                    if (active) {
                        navigation.showError("Dashboard",
                                "Could not load administrator statistics.");
                    }
                })
                .start();
    }

    private Statistics loadStats() {
        List<User> users = userService.getAllUsers();
        List<Quiz> quizzes = quizService.getAllQuizzes();
        List<Result> results = resultService.getAllResults();

        Statistics stats = new Statistics();
        stats.students = GenericUtils.count(users, user -> !user.isAdmin());
        stats.quizzes = quizzes.size();
        for (Quiz quiz : quizzes) {
            stats.questions += quiz.getTotalQuestions();
        }
        stats.attempts = results.size();
        double total = 0.0;
        for (Result result : results) {
            total += result.getPercentage();
        }
        stats.average = results.isEmpty() ? 0.0 : total / results.size();

        Map<Integer, String> studentNames = new HashMap<>();
        for (User user : users) {
            studentNames.put(user.getId(), user.getName());
        }
        Map<Integer, String> quizTitles = new HashMap<>();
        for (Quiz quiz : quizzes) {
            quizTitles.put(quiz.getId(), quiz.getTitle());
        }
        stats.results = results;
        stats.studentNames = studentNames;
        stats.quizTitles = quizTitles;
        return stats;
    }

    private static final class Statistics {

        private int students;
        private int quizzes;
        private int questions;
        private int attempts;
        private double average;
        private List<Result> results;
        private Map<Integer, String> studentNames;
        private Map<Integer, String> quizTitles;
    }
}
