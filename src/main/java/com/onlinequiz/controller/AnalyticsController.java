package com.onlinequiz.controller;

import com.onlinequiz.model.Quiz;
import com.onlinequiz.model.Result;
import com.onlinequiz.model.User;
import com.onlinequiz.navigation.ScreenLifecycle;
import com.onlinequiz.service.QuizService;
import com.onlinequiz.service.ResultService;
import com.onlinequiz.service.UserService;
import com.onlinequiz.threads.BackgroundTask;
import com.onlinequiz.view.AnalyticsView;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Administrator results workspace. Builds all-results, student-wise, quiz-wise and
 * pass/fail views from the same snapshot so the tabs never disagree.
 */
public final class AnalyticsController implements ScreenLifecycle {

    private final int initialTab;
    private final UserService userService;
    private final QuizService quizService;
    private final ResultService resultService;
    private final AnalyticsView view;

    private volatile boolean active = true;
    private boolean loading;

    public AnalyticsController(UserService userService, QuizService quizService,
                               ResultService resultService, AnalyticsView view,
                               int initialTab) {
        this.initialTab = initialTab;
        this.userService = userService;
        this.quizService = quizService;
        this.resultService = resultService;
        this.view = view;

        view.getRefreshButton().setOnAction(event -> load());
        load();
    }

    @Override
    public void onHide() {
        active = false;
    }

    private void load() {
        if (loading) {
            return;
        }
        loading = true;
        view.getRefreshButton().setDisable(true);
        new BackgroundTask<>(this::loadData, Platform::runLater)
                .onSuccess(data -> {
                    if (!active) {
                        return;
                    }
                    loading = false;
                    view.getRefreshButton().setDisable(false);
                    view.clearMessage();
                    view.setStats(data.attempts, data.average, data.highest,
                            data.passed, data.failed);
                    view.setResults(data.results, data.studentNames, data.quizTitles);
                    view.setStudentSummaries(data.studentSummaries);
                    view.setQuizSummaries(data.quizSummaries);
                    view.selectTab(initialTab);
                })
                .onFailure(error -> {
                    if (!active) {
                        return;
                    }
                    loading = false;
                    view.getRefreshButton().setDisable(false);
                    view.setError("Could not load results and analytics. Please refresh.");
                })
                .start();
    }

    private AnalyticsData loadData() {
        AnalyticsData data = new AnalyticsData();

        List<User> users = userService.getAllUsers();
        for (User user : users) {
            data.studentNames.put(user.getId(), user.getName());
        }

        List<Quiz> quizzes = quizService.getAllQuizzes();
        for (Quiz quiz : quizzes) {
            data.quizTitles.put(quiz.getId(), quiz.getTitle());
        }

        List<Result> results = new ArrayList<>(resultService.getAllResults());
        results.sort(Comparator.comparingInt(Result::getId).reversed());
        data.results = results;
        data.attempts = results.size();

        double total = 0.0;
        for (Result result : results) {
            total += result.getPercentage();
            data.highest = Math.max(data.highest, result.getPercentage());
            if (result.isPassed()) {
                data.passed++;
            }
        }
        data.average = data.attempts == 0 ? 0.0 : total / data.attempts;
        data.failed = data.attempts - data.passed;

        Map<Integer, List<Result>> byStudent = new HashMap<>();
        Map<Integer, List<Result>> byQuiz = new HashMap<>();
        for (Result result : results) {
            byStudent.computeIfAbsent(result.getUserId(), key -> new ArrayList<>())
                    .add(result);
            byQuiz.computeIfAbsent(result.getQuizId(), key -> new ArrayList<>())
                    .add(result);
        }

        for (User user : users) {
            if (user.isAdmin()) {
                continue;
            }
            List<Result> attempts = byStudent.getOrDefault(user.getId(),
                    java.util.Collections.emptyList());
            double sum = 0.0;
            double best = 0.0;
            int passed = 0;
            for (Result result : attempts) {
                sum += result.getPercentage();
                best = Math.max(best, result.getPercentage());
                if (result.isPassed()) {
                    passed++;
                }
            }
            data.studentSummaries.add(new AnalyticsView.StudentSummary(
                    user.getId(), user.getName(), attempts.size(),
                    attempts.isEmpty() ? 0.0 : sum / attempts.size(), best, passed));
        }
        data.studentSummaries.sort(Comparator.comparing(
                AnalyticsView.StudentSummary::getName, String.CASE_INSENSITIVE_ORDER));

        for (Quiz quiz : quizzes) {
            List<Result> attempts = byQuiz.getOrDefault(quiz.getId(),
                    java.util.Collections.emptyList());
            double sum = 0.0;
            double best = 0.0;
            for (Result result : attempts) {
                sum += result.getPercentage();
                best = Math.max(best, result.getPercentage());
            }
            data.quizSummaries.add(new AnalyticsView.QuizSummary(
                    quiz.getId(), quiz.getTitle(), quiz.getCategory(), attempts.size(),
                    attempts.isEmpty() ? 0.0 : sum / attempts.size(), best));
        }
        data.quizSummaries.sort(Comparator.comparing(
                AnalyticsView.QuizSummary::getTitle, String.CASE_INSENSITIVE_ORDER));
        return data;
    }

    private static final class AnalyticsData {

        private List<Result> results = new ArrayList<>();
        private final Map<Integer, String> studentNames = new HashMap<>();
        private final Map<Integer, String> quizTitles = new HashMap<>();
        private final List<AnalyticsView.StudentSummary> studentSummaries = new ArrayList<>();
        private final List<AnalyticsView.QuizSummary> quizSummaries = new ArrayList<>();
        private int attempts;
        private int passed;
        private int failed;
        private double average;
        private double highest;
    }
}
