package com.onlinequiz.generics;

import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.interfaces.Identifiable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic in-memory repository with one type parameter.
 *
 * The bound {@code T extends Identifiable} is a bounded type parameter: it allows
 * the repository to call getId() while still working for any stored domain object
 * (Quiz, Question, Result, ...). Insertion order is preserved by LinkedHashMap.
 *
 * @param <T> persisted domain type handled by this repository
 */
public class GenericRepository<T extends Identifiable> {

    private final Map<Integer, T> items = new LinkedHashMap<>();

    public void add(T item) {
        if (item == null) {
            throw new ValidationException("Item is required");
        }
        if (item.getId() <= 0) {
            throw new ValidationException("Only persisted items can be cached");
        }
        items.put(item.getId(), item);
    }

    public T getById(int id) {
        return items.get(id);
    }

    public List<T> getAll() {
        return new ArrayList<>(items.values());
    }

    public boolean remove(int id) {
        return items.remove(id) != null;
    }

    public boolean contains(int id) {
        return items.containsKey(id);
    }

    public int size() {
        return items.size();
    }

    public void clear() {
        items.clear();
    }
}
