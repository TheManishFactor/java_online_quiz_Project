package com.onlinequiz.navigation;

/**
 * Optional lifecycle hook for a screen controller. Navigation calls onHide()
 * before replacing the current screen, which lets background work and the quiz
 * countdown stop when the user leaves.
 */
public interface ScreenLifecycle {

    default void onHide() {
    }
}
