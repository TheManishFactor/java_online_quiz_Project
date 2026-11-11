package com.onlinequiz.collections;

import com.onlinequiz.model.Question;
import com.onlinequiz.model.Result;
import com.onlinequiz.model.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Executable tour of the Java Collections Framework using quiz domain objects.
 *
 * Run with:
 * java -cp "target/classes;target/test-classes" com.onlinequiz.collections.CollectionsDemo
 */
public final class CollectionsDemo {

    private CollectionsDemo() {
    }

    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println(" PHASE 4 - JAVA COLLECTIONS FRAMEWORK");
        System.out.println("=====================================================");

        demonstrateArrayList();
        demonstrateLinkedList();
        demonstrateVector();
        demonstrateHashSet();
        demonstrateTreeSet();
        demonstrateHashMap();
        demonstrateHashtable();
        demonstrateIterator();

        System.out.println();
        System.out.println("COLLECTIONS DEMONSTRATION FINISHED.");
    }

    /**
     * 1. ArrayList: a resizable array-backed List. It keeps insertion order,
     * allows duplicates and supports fast indexed access.
     */
    private static void demonstrateArrayList() {
        section("1. ArrayList - quiz questions");
        List<Question> questions = new ArrayList<>();

        questions.add(new Question(1, "Which keyword inherits a class?",
                "this", "super", "extends", "final", "C"));
        questions.add(new Question(1, "Which method starts a thread?",
                "run()", "start()", "execute()", "begin()", "B"));
        questions.add(new Question(1, "Which collection rejects duplicates?",
                "ArrayList", "LinkedList", "HashSet", "Vector", "C"));
        System.out.println("add(): " + questions.size() + " questions stored");

        Question first = questions.get(0);
        System.out.println("get(0): " + first.getQuestionText());

        Question old = questions.set(1, new Question(1, "Which interface has a key-value pair?",
                "List", "Set", "Queue", "Map", "D"));
        System.out.println("set(1) replaced: " + old.getQuestionText());

        questions.remove(0);
        System.out.println("remove(0): size is now " + questions.size());

        System.out.println("Iteration:");
        for (Question question : questions) {
            System.out.println("  - " + question.getQuestionText());
        }
    }

    /**
     * 2. LinkedList: a doubly linked list. Adding or removing at the ends is
     * cheap, which makes it a natural fit for next/previous question navigation.
     */
    private static void demonstrateLinkedList() {
        section("2. LinkedList - question navigation");
        LinkedList<Question> navigation = new LinkedList<>();
        Question q1 = new Question(2, "What is a variable?",
                "A value", "A named storage location", "A method", "A class", "B");
        Question q2 = new Question(2, "What is a loop?",
                "A decision", "A repetition", "A class", "A package", "B");
        Question q3 = new Question(2, "What is inheritance?",
                "Reusing a parent type", "Deleting code", "A loop", "An array", "A");

        navigation.addLast(q1);
        navigation.addLast(q2);
        navigation.addFirst(q3);
        System.out.println("First question: " + navigation.getFirst().getQuestionText());
        System.out.println("Last question : " + navigation.getLast().getQuestionText());
        System.out.println("Size          : " + navigation.size());

        System.out.println("removeFirst(): " + navigation.removeFirst().getQuestionText());
        System.out.println("removeLast() : " + navigation.removeLast().getQuestionText());
        System.out.println("Remaining    : " + navigation.getFirst().getQuestionText());
    }

    /**
     * 3. Vector: a synchronized, resizable List from the original Java collection
     * API. It behaves like ArrayList but is thread-safe for individual operations.
     */
    private static void demonstrateVector() {
        section("3. Vector - quiz results");
        Vector<Result> results = new Vector<>();
        results.add(new Result(1, 1, 1, 8, 10, 80.0, "2026-11-11"));
        results.add(new Result(2, 1, 2, 6, 10, 60.0, "2026-11-12"));
        results.add(new Result(3, 2, 1, 9, 10, 90.0, "2026-11-13"));
        System.out.println("add(): size = " + results.size());

        Result first = results.get(0);
        System.out.println("get(0): user " + first.getUserId()
                + " scored " + first.getScoreLabel());

        Result removed = results.remove(1);
        System.out.println("remove(1): removed result #" + removed.getId());
        System.out.println("size() after removal = " + results.size());
    }

    /**
     * 4. HashSet: a Set backed by a hash table. It stores each value only once,
     * which is ideal for unique email addresses.
     */
    private static void demonstrateHashSet() {
        section("4. HashSet - unique student emails");
        Set<String> emails = new HashSet<>();
        System.out.println("add(asha) first time : " + emails.add("asha@example.com"));
        System.out.println("add(asha) again      : " + emails.add("asha@example.com"));
        System.out.println("add(rahul)           : " + emails.add("rahul@example.com"));
        System.out.println("Unique emails        : " + emails.size());
        System.out.println("Contains asha        : " + emails.contains("asha@example.com"));
    }

    /**
     * 5. TreeSet: a sorted Set. Duplicate categories are ignored automatically and
     * iteration always happens in alphabetical order.
     */
    private static void demonstrateTreeSet() {
        section("5. TreeSet - sorted quiz categories");
        TreeSet<String> categories = new TreeSet<>();
        categories.add("Programming");
        categories.add("General Knowledge");
        categories.add("Databases");
        categories.add("Programming");
        System.out.println("Size after duplicate add: " + categories.size());
        System.out.println("Sorted categories:");
        for (String category : categories) {
            System.out.println("  - " + category);
        }
        System.out.println("First: " + categories.first()
                + " | Last: " + categories.last());
    }

    /**
     * 6. HashMap: key-value pairs with constant-time lookup by key. Keys must be
     * unique, so a student id maps to exactly one student object.
     */
    private static void demonstrateHashMap() {
        section("6. HashMap - students by id");
        Map<Integer, Student> students = new HashMap<>();
        Student asha = new Student(1, "Asha Rao", "asha@example.com", "stored-hash");
        Student rahul = new Student(2, "Rahul Mehta", "rahul@example.com", "stored-hash");

        students.put(asha.getId(), asha);
        students.put(rahul.getId(), rahul);
        System.out.println("Size: " + students.size());
        System.out.println("get(1): " + students.get(1).getName());
        System.out.println("containsKey(2): " + students.containsKey(2));
        System.out.println("containsKey(99): " + students.containsKey(99));

        for (Map.Entry<Integer, Student> entry : students.entrySet()) {
            System.out.println("  " + entry.getKey() + " -> " + entry.getValue().getName());
        }
    }

    /**
     * 7. Hashtable: an older synchronized Map implementation. Unlike HashMap it
     * rejects null keys and null values.
     */
    private static void demonstrateHashtable() {
        section("7. Hashtable - quiz statistics");
        Hashtable<String, Integer> statistics = new Hashtable<>();
        statistics.put("Java Basics attempts", 14);
        statistics.put("Java Basics passes", 11);
        statistics.put("World Capitals attempts", 9);

        System.out.println("Java Basics attempts: "
                + statistics.get("Java Basics attempts"));
        System.out.println("Size: " + statistics.size());
        statistics.remove("World Capitals attempts");
        System.out.println("After remove, containsKey: "
                + statistics.containsKey("World Capitals attempts"));
        System.out.println("Statistics:");
        for (Map.Entry<String, Integer> entry : statistics.entrySet()) {
            System.out.println("  " + entry.getKey() + " = " + entry.getValue());
        }
        System.out.println("Hashtable does not accept null keys or values.");
    }

    /**
     * 8. Iterator: the standard cursor for traversing any Collection. It exposes
     * hasNext() and next(), and supports safe removal during iteration.
     */
    private static void demonstrateIterator() {
        section("8. Iterator - traversing questions");
        List<Question> questions = new ArrayList<>();
        questions.add(new Question(3, "What is a class?",
                "A blueprint", "A loop", "A value", "A package", "A"));
        questions.add(new Question(3, "What is an object?",
                "An instance of a class", "A keyword", "A file", "An index", "A"));
        questions.add(new Question(3, "What is encapsulation?",
                "Hiding internal state", "Looping", "Inheriting", "Casting", "A"));

        Iterator<Question> iterator = questions.iterator();
        int number = 1;
        while (iterator.hasNext()) {
            Question question = iterator.next();
            System.out.println("  " + number++ + ". " + question.getQuestionText());
        }
        System.out.println("Iterator traversed " + questions.size() + " questions.");
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("--- " + title + " ---");
    }
}
