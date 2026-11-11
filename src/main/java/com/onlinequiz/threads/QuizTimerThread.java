package com.onlinequiz.threads;

/**
 * Countdown timer for a quiz attempt.
 *
 * The class implements Runnable and runs on its own daemon Thread, so the JavaFX
 * Application Thread is never blocked by sleeps. Listeners are invoked from the
 * timer thread; the controller forwards them to the UI with Platform.runLater().
 *
 * The running flag and remaining seconds are volatile because another thread
 * (the UI thread) calls stop() while the timer thread is sleeping.
 */
public final class QuizTimerThread implements Runnable {

    /**
     * Receives countdown updates. Implementations that touch JavaFX must switch to
     * the Application Thread themselves.
     */
    public interface Listener {

        void onTick(int secondsRemaining);

        void onTimeExpired();
    }

    private final int totalSeconds;
    private final Listener listener;

    private volatile int remainingSeconds;
    private volatile boolean running;
    private Thread worker;

    public QuizTimerThread(int totalSeconds, Listener listener) {
        if (totalSeconds <= 0) {
            throw new IllegalArgumentException("Timer length must be positive");
        }
        if (listener == null) {
            throw new IllegalArgumentException("Timer listener is required");
        }
        this.totalSeconds = totalSeconds;
        this.remainingSeconds = totalSeconds;
        this.listener = listener;
    }

    public synchronized void start() {
        if (worker != null) {
            throw new IllegalStateException("This timer has already been started");
        }
        running = true;
        worker = new Thread(this, "quiz-countdown");
        worker.setDaemon(true);
        worker.start();
    }

    @Override
    public void run() {
        listener.onTick(remainingSeconds);
        while (running && remainingSeconds > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                break;
            }
            if (!running) {
                break;
            }
            remainingSeconds--;
            listener.onTick(remainingSeconds);
        }
        if (running && remainingSeconds == 0) {
            running = false;
            listener.onTimeExpired();
        }
    }

    /**
     * Stops the countdown. Safe to call more than once and safe to call after the
     * timer has already finished.
     */
    public void stop() {
        running = false;
        Thread thread = worker;
        if (thread != null) {
            thread.interrupt();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public int getTotalSeconds() {
        return totalSeconds;
    }
}
