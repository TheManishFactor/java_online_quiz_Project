package com.onlinequiz.generics;

/**
 * Immutable pair with two independent type parameters.
 *
 * Example: {@code GenericPair<Integer, String>} can hold a student id and name
 * without ever mixing up which value is which, something an Object[] cannot
 * guarantee.
 *
 * @param <F> first value type
 * @param <S> second value type
 */
public final class GenericPair<F, S> {

    private final F first;
    private final S second;

    public GenericPair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public static <F, S> GenericPair<F, S> of(F first, S second) {
        return new GenericPair<>(first, second);
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    /**
     * Returns a new pair with the values exchanged and the type parameters swapped.
     */
    public GenericPair<S, F> swap() {
        return new GenericPair<>(second, first);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof GenericPair)) {
            return false;
        }
        GenericPair<?, ?> pair = (GenericPair<?, ?>) other;
        return (first == null ? pair.first == null : first.equals(pair.first))
                && (second == null ? pair.second == null : second.equals(pair.second));
    }

    @Override
    public int hashCode() {
        int result = first == null ? 0 : first.hashCode();
        return 31 * result + (second == null ? 0 : second.hashCode());
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
