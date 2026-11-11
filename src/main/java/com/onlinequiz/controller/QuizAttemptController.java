package com.onlinequiz.controller;

import com.onlinequiz.exception.QuizNotFoundException;
import com.onlinequiz.exception.QuizSubmissionException;
import com.onlinequiz.model.Quiz;
import com.onlinequiz.model.Result;
import com.onlinequiz.model.User;
import com.onlinequiz.navigation.NavigationManager;
import com.onlinequiz.navigation.ScreenLifecycle;
import com.onlinequiz.service.ResultService;
import com.onlinequiz.threads.QuizTimerThread;
import com.onlinequiz.util.ExceptionHandler;
import com.onlinequiz.view.QuizAttemptView;
import com.onlinequiz.view.UiFactory;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Runs one quiz attempt: question navigation, answer preservation, countdown,
 * automatic expiry submission and duplicate-submission protection.
 */
public final class QuizAttemptController implements ScreenLifecycle {

    private static final DateTimeFormatter ATTEMPT_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final NavigationManager navigation;
    private final User student;
    private final Quiz quiz;
    private final ResultService resultService;
    private final QuizAttemptView view;
    private final QuizAttemptSession session;
    private final AttemptSubmission submission = new AttemptSubmission();
    private final long startedAtNanos = System.nanoTime();

    private QuizTimerThread timer;
    private volatile boolean active = true;
    private boolean dialogOpen;

    public QuizAttemptController(NavigationManager navigation, ResultService resultService,
                                 User student, Quiz quiz, QuizAttemptView view) {
        this.navigation = navigation;
        this.resultService = resultService;
        this.student = student;
        this.quiz = quiz;
        this.view = view;
        this.session = new QuizAttemptSession(quiz);

        view.setQuizTitle(quiz.getTitle());
        view.setOnOptionSelected(this::selectAnswer);
        view.getPreviousButton().setOnAction(event -> previous());
        view.getNextButton().setOnAction(event -> next());
        view.getSubmitButton().setOnAction(event -> confirmSubmit());
        view.addEventFilter(KeyEvent.KEY_PRESSED, this::handleShortcut);

        renderQuestion();
        view.setProgress(0, session.getTotalQuestions());
        view.setTimer(formatTime(quiz.getDuration() * 60));
        startTimer(quiz.getDuration() * 60);
        Platform.runLater(view::requestFocus);
    }

    @Override
    public void onHide() {
        active = false;
        if (timer != null) {
            timer.stop();
        }
    }

    private void startTimer(int seconds) {
        timer = new QuizTimerThread(seconds, new QuizTimerThread.Listener() {
            @Override
            public void onTick(int secondsRemaining) {
                Platform.runLater(() -> {
                    if (active) {
                        view.setTimer(formatTime(secondsRemaining));
                    }
                });
            }

            @Override
            public void onTimeExpired() {
                Platform.runLater(() -> {
                    if (active) {
                        submitAttempt(true);
                    }
                });
            }
        });
        timer.start();
    }

    private void selectAnswer(String label) {
        if (!active || submission.isSubmitted()) {
            return;
        }
        session.selectAnswer(label);
        updateProgress();
        view.clearMessage();
    }

    private void previous() {
        session.previous();
        renderQuestion();
    }

    private void next() {
        session.next();
        renderQuestion();
    }

    private void renderQuestion() {
        view.renderQuestion(session.getCurrentQuestion(), session.getCurrentIndex(),
                session.getTotalQuestions(), session.getCurrentAnswer());
        view.setNavigationEnabled(session.canGoPrevious(), session.canGoNext());
        updateProgress();
    }

    private void updateProgress() {
        view.setProgress(session.getAnsweredCount(), session.getTotalQuestions());
    }

    private void handleShortcut(KeyEvent event) {
        if (!active || submission.isSubmitted() || dialogOpen) {
            return;
        }
        if (event.getCode() == KeyCode.LEFT) {
            previous();
            event.consume();
        } else if (event.getCode() == KeyCode.RIGHT) {
            next();
            event.consume();
        } else if (event.getCode() == KeyCode.DIGIT1) {
            view.selectOption(0);
            event.consume();
        } else if (event.getCode() == KeyCode.DIGIT2) {
            view.selectOption(1);
            event.consume();
        } else if (event.getCode() == KeyCode.DIGIT3) {
            view.selectOption(2);
            event.consume();
        } else if (event.getCode() == KeyCode.DIGIT4) {
            view.selectOption(3);
            event.consume();
        }
    }

    private void confirmSubmit() {
        if (!active || submission.isSubmitted() || dialogOpen) {
            return;
        }
        if (session.getAnsweredCount() == 0) {
            view.setError("Select at least one answer before submitting.");
            return;
        }
        dialogOpen = true;
        boolean confirmed;
        try {
            confirmed = UiFactory.confirm(navigation.getStage(), "Submit attempt",
                    "Submit this attempt now?",
                    "You have answered " + session.getAnsweredCount() + " of "
                            + session.getTotalQuestions() + " questions. This cannot be undone.");
        } finally {
            dialogOpen = false;
        }
        if (confirmed) {
            submitAttempt(false);
        }
    }

    /**
     * Called by the Submit button (manual) and by timer expiry (automatic). The
     * AttemptSubmission guard makes the second call a no-op once a save succeeded.
     */
    private void submitAttempt(boolean automatic) {
        if (!active || submission.isSubmitted()) {
            return;
        }
        if (!automatic && dialogOpen) {
            return;
        }
        if (!automatic && session.getAnsweredCount() == 0) {
            view.setError("Select at least one answer before submitting.");
            return;
        }

        timer.stop();
        view.setSubmitting(true);
        view.clearMessage();

        String attemptDate = LocalDateTime.now().format(ATTEMPT_DATE);
        try {
            Result result = submission.submit(student.getId(), quiz, session.copyAnswers(),
                    attemptDate, automatic, resultService);
            long seconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startedAtNanos);
            navigation.showResult(quiz, result, session.copyAnswers(), seconds);
        } catch (QuizSubmissionException | QuizNotFoundException error) {
            view.setSubmitting(false);
            view.setError(ExceptionHandler.getUserMessage(error));
            resumeTimerIfPossible();
        } catch (RuntimeException error) {
            ExceptionHandler.handle(error, "Quiz submission");
            view.setSubmitting(false);
            view.setError("The attempt could not be saved. Please try submitting again.");
            resumeTimerIfPossible();
        }
    }

    private void resumeTimerIfPossible() {
        if (timer != null && timer.getRemainingSeconds() > 0) {
            startTimer(timer.getRemainingSeconds());
        } else {
            view.setNavigationEnabled(session.canGoPrevious(), session.canGoNext());
        }
    }

    private static String formatTime(int seconds) {
        int safe = Math.max(0, seconds);
        return String.format("%02d:%02d", safe / 60, safe % 60);
    }
}
