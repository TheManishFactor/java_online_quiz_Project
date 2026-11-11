package com.onlinequiz.controller;

import com.onlinequiz.model.Quiz;
import com.onlinequiz.model.Result;
import com.onlinequiz.model.User;
import com.onlinequiz.navigation.NavigationManager;
import com.onlinequiz.navigation.ScreenLifecycle;
import com.onlinequiz.service.QuizService;
import com.onlinequiz.service.ResultService;
import com.onlinequiz.service.UserService;
import com.onlinequiz.threads.BackgroundTask;
import com.onlinequiz.view.HistoryView;
import com.onlinequiz.view.StudentManagementView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Administrator student directory. Student accounts and their attempts are read
 * through the services on a background thread; only display fields are handed to
 * the view, so password hashes never reach the screen.
 */
public final class StudentManagementController implements ScreenLifecycle {

    private final UserService userService;
    private final ResultService resultService;
    private final QuizService quizService;
    private final StudentManagementView view;

    private final ObservableList<User> allStudents = FXCollections.observableArrayList();
    private final FilteredList<User> filtered = new FilteredList<>(allStudents, student -> true);

    private volatile boolean active = true;
    private boolean loading;

    private final Map<Integer, StudentStats> statsById = new HashMap<>();
    private final Map<Integer, List<HistoryView.HistoryRow>> rowsById = new HashMap<>();

    public StudentManagementController(NavigationManager navigation, UserService userService,
                                       ResultService resultService, QuizService quizService,
                                       StudentManagementView view) {
        this.userService = userService;
        this.resultService = resultService;
        this.quizService = quizService;
        this.view = view;

        view.getRefreshButton().setOnAction(event -> load());
        view.getSearchField().textProperty().addListener(
                (observable, previous, text) -> applyFilter(text));
        view.getTable().getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, student) -> showDetails(student));

        view.setTableItems(filtered);
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
                    statsById.clear();
                    statsById.putAll(data.statsById);
                    rowsById.clear();
                    rowsById.putAll(data.rowsById);
                    allStudents.setAll(data.students);
                    applyFilter(view.getSearchField().getText());
                    view.clearDetails();
                    view.getRefreshButton().setDisable(false);
                    loading = false;
                })
                .onFailure(error -> {
                    if (!active) {
                        return;
                    }
                    loading = false;
                    view.getRefreshButton().setDisable(false);
                    view.setMessage("Could not load students. Please refresh.", true);
                })
                .start();
    }

    private StudentData loadData() {
        StudentData data = new StudentData();
        Map<Integer, String> quizTitles = new HashMap<>();
        for (Quiz quiz : quizService.getAllQuizzes()) {
            quizTitles.put(quiz.getId(), quiz.getTitle());
        }
        for (User user : userService.getAllUsers()) {
            if (!user.isAdmin()) {
                data.students.add(user);
            }
        }

        Map<Integer, List<Result>> byStudent = new HashMap<>();
        for (Result result : resultService.getAllResults()) {
            byStudent.computeIfAbsent(result.getUserId(), key -> new ArrayList<>()).add(result);
        }

        for (User student : data.students) {
            List<Result> results = byStudent.getOrDefault(student.getId(),
                    Collections.emptyList());
            List<Result> sorted = new ArrayList<>(results);
            sorted.sort(Comparator.comparingInt(Result::getId).reversed());

            List<HistoryView.HistoryRow> rows = new ArrayList<>();
            double total = 0.0;
            double best = 0.0;
            int passed = 0;
            for (Result result : sorted) {
                total += result.getPercentage();
                best = Math.max(best, result.getPercentage());
                if (result.isPassed()) {
                    passed++;
                }
                rows.add(new HistoryView.HistoryRow(
                        quizTitles.getOrDefault(result.getQuizId(),
                                "Quiz " + result.getQuizId()),
                        result.getScore(),
                        result.getTotalQuestions(),
                        result.getPercentage(),
                        result.getGrade(),
                        result.getAttemptDate(),
                        result.isPassed()));
            }
            data.rowsById.put(student.getId(), rows);
            data.statsById.put(student.getId(), new StudentStats(rows.size(),
                    rows.isEmpty() ? 0.0 : total / rows.size(), best, passed));
        }
        return data;
    }

    private void applyFilter(String text) {
        String needle = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        filtered.setPredicate(student -> needle.isEmpty()
                || student.getName().toLowerCase(Locale.ROOT).contains(needle)
                || student.getEmail().toLowerCase(Locale.ROOT).contains(needle));
    }

    private void showDetails(User student) {
        if (student == null) {
            view.clearDetails();
            return;
        }
        StudentStats stats = statsById.get(student.getId());
        if (stats == null) {
            view.clearDetails();
            return;
        }
        view.showDetails(student, rowsById.getOrDefault(student.getId(),
                        Collections.emptyList()),
                stats.attempts, stats.average, stats.best, stats.passed);
    }

    private static final class StudentStats {

        private final int attempts;
        private final double average;
        private final double best;
        private final int passed;

        private StudentStats(int attempts, double average, double best, int passed) {
            this.attempts = attempts;
            this.average = average;
            this.best = best;
            this.passed = passed;
        }
    }

    private static final class StudentData {

        private final List<User> students = new ArrayList<>();
        private final Map<Integer, StudentStats> statsById = new HashMap<>();
        private final Map<Integer, List<HistoryView.HistoryRow>> rowsById = new HashMap<>();
    }
}
