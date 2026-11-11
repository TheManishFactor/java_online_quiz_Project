package com.onlinequiz.threads;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Runs slow work on a background thread and delivers the outcome through a
 * caller-supplied UI executor (for JavaFX controllers: {@code Platform::runLater}).
 *
 * Why this exists: database queries, file exports and result loading can take long
 * enough to freeze the JavaFX Application Thread. The caller keeps the UI
 * responsive and receives both success and failure callbacks on the UI thread.
 *
 * @param <T> result type produced by the background work
 */
public final class BackgroundTask<T> {

    private final Callable<T> work;
    private final Executor uiExecutor;

    private Consumer<T> onSuccess = value -> {
    };
    private Consumer<Throwable> onFailure = error -> {
    };
    private Runnable onFinished = () -> {
    };
    private boolean started;

    public BackgroundTask(Callable<T> work, Executor uiExecutor) {
        if (work == null || uiExecutor == null) {
            throw new IllegalArgumentException("Work and UI executor are required");
        }
        this.work = work;
        this.uiExecutor = uiExecutor;
    }

    public BackgroundTask<T> onSuccess(Consumer<T> handler) {
        this.onSuccess = handler == null ? value -> {
        } : handler;
        return this;
    }

    public BackgroundTask<T> onFailure(Consumer<Throwable> handler) {
        this.onFailure = handler == null ? error -> {
        } : handler;
        return this;
    }

    public BackgroundTask<T> onFinished(Runnable handler) {
        this.onFinished = handler == null ? () -> {
        } : handler;
        return this;
    }

    /**
     * Starts the worker. The returned thread is a daemon so a forgotten task can
     * never keep the desktop application alive after the window is closed.
     */
    public synchronized Thread start() {
        if (started) {
            throw new IllegalStateException("This background task has already started");
        }
        started = true;
        Thread worker = new Thread(this::runWork, "quiz-background-task");
        worker.setDaemon(true);
        worker.start();
        return worker;
    }

    private void runWork() {
        try {
            T value = work.call();
            uiExecutor.execute(() -> {
                try {
                    onSuccess.accept(value);
                } finally {
                    onFinished.run();
                }
            });
        } catch (Throwable error) {
            uiExecutor.execute(() -> {
                try {
                    onFailure.accept(error);
                } finally {
                    onFinished.run();
                }
            });
        }
    }
}
