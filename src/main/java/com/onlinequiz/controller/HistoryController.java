package com.onlinequiz.controller;

import com.onlinequiz.io.ResultExporter;
import com.onlinequiz.model.Quiz;
import com.onlinequiz.model.Result;
import com.onlinequiz.model.User;
import com.onlinequiz.navigation.NavigationManager;
import com.onlinequiz.navigation.ScreenLifecycle;
import com.onlinequiz.service.QuizService;
import com.onlinequiz.service.ResultService;
import com.onlinequiz.threads.BackgroundTask;
import com.onlinequiz.view.HistoryView;
import javafx.application.Platform;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads the signed-in student's attempt history and exports it as a text report.
 * Both database access and file writing run on background threads; the view is
 * only touched from the JavaFX Application Thread.
 */
public final class HistoryController implements ScreenLifecycle {

    private final NavigationManager navigation;
    private final User student;
    private final ResultService resultService;
    private final QuizService quizService;
    private final HistoryView view;
    private final ResultExporter exporter = new ResultExporter();

    private volatile boolean active = true;
    private List<Result> results = new ArrayList<>();
    private Map<Integer, String> quizTitles = new HashMap<>();

    public HistoryController(NavigationManager navigation, ResultService resultService,
                             QuizService quizService, User student, HistoryView view) {
        this.navigation = navigation;
        this.resultService = resultService;
        this.quizService = quizService;
        this.student = student;
        this.view = view;

        view.getRefreshButton().setOnAction(event -> load());
        view.getExportButton().setOnAction(event -> exportResults());
        load();
    }

    @Override
    public void onHide() {
        active = false;
    }

    private void load() {
        view.setBusy(true);
        view.clearMessage();
        new BackgroundTask<>(this::loadData, Platform::runLater)
                .onSuccess(data -> {
                    if (!active) {
                        return;
                    }
                    results = data.results;
                    quizTitles = data.quizTitles;
                    view.setSummary(data.attempts, data.average, data.best, data.passed);
                    view.setRows(data.rows);
                    view.setBusy(false);
                })
                .onFailure(error -> {
                    if (!active) {
                        return;
                    }
                    view.setBusy(false);
                    view.setMessage("Could not load your results. Please refresh.", true);
                })
                .start();
    }

    private HistoryData loadData() {
        HistoryData data = new HistoryData();
        data.results = new ArrayList<>(resultService.getResultsByStudentId(student.getId()));
        data.results.sort(Comparator.comparingInt(Result::getId).reversed());

        for (Quiz quiz : quizService.getAllQuizzes()) {
            data.quizTitles.put(quiz.getId(), quiz.getTitle());
        }

        double total = 0.0;
        for (Result result : data.results) {
            total += result.getPercentage();
            data.best = Math.max(data.best, result.getPercentage());
            if (result.isPassed()) {
                data.passed++;
            }
            data.rows.add(new HistoryView.HistoryRow(
                    data.quizTitles.getOrDefault(result.getQuizId(),
                            "Quiz " + result.getQuizId()),
                    result.getScore(),
                    result.getTotalQuestions(),
                    result.getPercentage(),
                    result.getGrade(),
                    result.getAttemptDate(),
                    result.isPassed()));
        }
        data.attempts = data.results.size();
        data.average = data.attempts == 0 ? 0.0 : total / data.attempts;
        return data;
    }

    private void exportResults() {
        if (results.isEmpty()) {
            view.setMessage("There are no attempts to export yet.", true);
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export quiz results");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text files", "*.txt"));
        Path defaultFile = ResultExporter.defaultFile(student);
        chooser.setInitialFileName(defaultFile.getFileName().toString());
        Path parent = defaultFile.toAbsolutePath().getParent();
        if (parent != null && Files.isDirectory(parent)) {
            chooser.setInitialDirectory(parent.toFile());
        }

        File chosen = chooser.showSaveDialog(navigation.getStage());
        if (chosen == null) {
            return;
        }

        Path target = chosen.toPath();
        view.setBusy(true);
        view.clearMessage();
        List<Result> snapshot = new ArrayList<>(results);
        Map<Integer, String> titles = new HashMap<>(quizTitles);
        new BackgroundTask<>(() -> exporter.exportStudentResults(target, student, snapshot, titles),
                Platform::runLater)
                .onSuccess(file -> {
                    if (!active) {
                        return;
                    }
                    view.setBusy(false);
                    view.setMessage("Results exported to " + file, false);
                })
                .onFailure(error -> {
                    if (!active) {
                        return;
                    }
                    view.setBusy(false);
                    view.setMessage("Could not export results: " + describe(error), true);
                })
                .start();
    }

    private static String describe(Throwable error) {
        String message = error.getMessage();
        return message == null || message.trim().isEmpty()
                ? error.getClass().getSimpleName()
                : message;
    }

    private static final class HistoryData {

        private List<Result> results = new ArrayList<>();
        private final List<HistoryView.HistoryRow> rows = new ArrayList<>();
        private final Map<Integer, String> quizTitles = new HashMap<>();
        private int attempts;
        private int passed;
        private double average;
        private double best;
    }
}
