package com.onlinequiz.generics;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Generic utility methods. The type parameter appears before the return type, so
 * every call infers its argument type and stays type-safe.
 */
public final class GenericUtils {

    private GenericUtils() {
    }

    /**
     * Prints every element in iteration order.
     *
     * @param title heading printed before the elements
     * @param items collection to display; may be empty
     * @param <T>   element type
     */
    public static <T> void display(String title, Iterable<T> items) {
        System.out.println(title);
        int index = 1;
        for (T item : items) {
            System.out.println("  " + index++ + ". " + item);
        }
        if (index == 1) {
            System.out.println("  (no elements)");
        }
    }

    /**
     * Returns the first element that matches the predicate, or null when none does.
     *
     * @param items     elements to search
     * @param predicate condition the element must satisfy
     * @param <T>       element type
     */
    public static <T> T find(Iterable<T> items, Predicate<T> predicate) {
        if (items == null || predicate == null) {
            return null;
        }
        for (T item : items) {
            if (predicate.test(item)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Counts the elements that match the predicate.
     */
    public static <T> int count(Iterable<T> items, Predicate<T> predicate) {
        if (items == null || predicate == null) {
            return 0;
        }
        int matches = 0;
        for (T item : items) {
            if (predicate.test(item)) {
                matches++;
            }
        }
        return matches;
    }

    /**
     * Bounded generic method: T must implement Comparable so the method can use
     * compareTo without casting. Returns null for an empty collection.
     */
    public static <T extends Comparable<T>> T max(Collection<T> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        T largest = null;
        for (T item : items) {
            if (item == null) {
                continue;
            }
            if (largest == null || item.compareTo(largest) > 0) {
                largest = item;
            }
        }
        return largest;
    }
}
