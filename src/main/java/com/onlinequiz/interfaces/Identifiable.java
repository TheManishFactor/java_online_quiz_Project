package com.onlinequiz.interfaces;

/**
 * Contract for domain objects that are stored with a database identifier.
 */
public interface Identifiable {

    int getId();

    default boolean isPersisted() {
        return getId() > 0;
    }
}
