package com.onlinequiz.util;

import com.onlinequiz.model.Question;
import com.onlinequiz.model.Result;

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
 * Console checks for every collection required by Phase 4.
 *
 * Run with:
 * java -cp "target/classes;target/test-classes" com.onlinequiz.util.CollectionsTest
 */
public final class CollectionsTest {

    private static int checks;
    private static int failures;

    private CollectionsTest() {
    }

    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println(" PHASE 4 - COLLECTIONS TESTS");
        System.out.println("=====================================================");

        testArrayList();
        testLinkedList();
        testVector();
        testHashSet();
        testTreeSet();
        testHashMap();
        testHashtable();
        testIterator();

        System.out.println();
        if (failures == 0) {
            System.out.println("ALL COLLECTION CHECKS PASSED (" + checks + " checks)");
        } else {
            System.out.println(failures + " OF " + checks + " COLLECTION CHECKS FAILED");
            System.exit(1);
        }
    }

    private static void testArrayList() {
        check("ArrayList add/get/set/remove/size and iteration", () -> {
            List<Question> questions = new ArrayList<>();
            questions.add(question("First"));
            questions.add(question("Second"));
            questions.add(question("Third"));
            require(questions.size() == 3, "size should be 3");
            require("First".equals(questions.get(0).getQuestionText()), "get(0) failed");

            questions.set(1, question("Replaced"));
            require("Replaced".equals(questions.get(1).getQuestionText()), "set(1) failed");

            questions.remove(0);
            require(questions.size() == 2, "remove should reduce size");

            int visited = 0;
            for (Question question : questions) {
                require(question != null, "iteration produced null");
                visited++;
            }
            require(visited == 2, "iteration should visit two questions");
        });
    }

    private static void testLinkedList() {
        check("LinkedList addFirst/addLast/getFirst/getLast/removeFirst/removeLast", () -> {
            LinkedList<Question> navigation = new LinkedList<>();
            navigation.addLast(question("Middle"));
            navigation.addLast(question("Last"));
            navigation.addFirst(question("First"));

            require("First".equals(navigation.getFirst().getQuestionText()), "getFirst failed");
            require("Last".equals(navigation.getLast().getQuestionText()), "getLast failed");
            require("First".equals(navigation.removeFirst().getQuestionText()),
                    "removeFirst failed");
            require("Last".equals(navigation.removeLast().getQuestionText()),
                    "removeLast failed");
            require(navigation.size() == 1, "one question should remain");
            require("Middle".equals(navigation.getFirst().getQuestionText()),
                    "remaining question should be Middle");
        });
    }

    private static void testVector() {
        check("Vector add/get/remove/size", () -> {
            Vector<Result> results = new Vector<>();
            results.add(new Result(1, 1, 1, 8, 10, 80.0, "2026-11-11"));
            results.add(new Result(2, 1, 2, 6, 10, 60.0, "2026-11-12"));

            require(results.size() == 2, "size should be 2");
            require(results.get(0).getScore() == 8, "get(0) failed");
            Result removed = results.remove(1);
            require(removed.getId() == 2, "remove(1) returned wrong result");
            require(results.size() == 1, "size should be 1 after removal");
        });
    }

    private static void testHashSet() {
        check("HashSet prevents duplicate emails", () -> {
            Set<String> emails = new HashSet<>();
            require(emails.add("asha@example.com"), "first add should succeed");
            require(!emails.add("asha@example.com"), "duplicate add should be rejected");
            require(emails.add("rahul@example.com"), "second unique add should succeed");
            require(emails.size() == 2, "duplicate email should not increase size");
        });
    }

    private static void testTreeSet() {
        check("TreeSet sorts categories and removes duplicates", () -> {
            TreeSet<String> categories = new TreeSet<>();
            categories.add("Programming");
            categories.add("General Knowledge");
            categories.add("Databases");
            categories.add("Programming");

            require(categories.size() == 3, "duplicate category should be ignored");
            require("Databases".equals(categories.first()), "first should be Databases");
            require("Programming".equals(categories.last()), "last should be Programming");

            List<String> order = new ArrayList<>(categories);
            require("Databases".equals(order.get(0)), "order[0] failed");
            require("General Knowledge".equals(order.get(1)), "order[1] failed");
            require("Programming".equals(order.get(2)), "order[2] failed");
        });
    }

    private static void testHashMap() {
        check("HashMap stores and retrieves student key-value pairs", () -> {
            Map<Integer, String> students = new HashMap<>();
            students.put(1, "Asha Rao");
            students.put(2, "Rahul Mehta");

            require(students.size() == 2, "size should be 2");
            require("Asha Rao".equals(students.get(1)), "get(1) failed");
            require(students.containsKey(2), "containsKey(2) failed");
            require(!students.containsKey(99), "containsKey(99) should be false");

            students.put(1, "Asha Rao Updated");
            require(students.size() == 2, "replacing a value must not add a key");
            require("Asha Rao Updated".equals(students.get(1)), "replace failed");
        });
    }

    private static void testHashtable() {
        check("Hashtable stores, removes and sizes quiz statistics", () -> {
            Hashtable<String, Integer> statistics = new Hashtable<>();
            statistics.put("attempts", 14);
            statistics.put("passes", 11);

            require(statistics.size() == 2, "size should be 2");
            require(statistics.get("attempts") == 14, "get failed");
            statistics.remove("passes");
            require(statistics.size() == 1, "size should be 1 after removal");
            require(!statistics.containsKey("passes"), "removed key must be absent");
        });
    }

    private static void testIterator() {
        check("Iterator traverses and removes elements safely", () -> {
            List<String> topics = new ArrayList<>();
            topics.add("Java");
            topics.add("JDBC");
            topics.add("Collections");

            int visited = 0;
            Iterator<String> iterator = topics.iterator();
            while (iterator.hasNext()) {
                require(iterator.next() != null, "iterator returned null");
                visited++;
            }
            require(visited == 3, "iterator should visit three elements");

            Iterator<String> removing = topics.iterator();
            while (removing.hasNext()) {
                if ("JDBC".equals(removing.next())) {
                    removing.remove();
                }
            }
            require(topics.size() == 2, "iterator.remove should remove one element");
            require(!topics.contains("JDBC"), "JDBC should have been removed");
        });
    }

    private static Question question(String text) {
        return new Question(1, text, "A", "B", "C", "D", "A");
    }

    private static void check(String description, Action action) {
        try {
            action.run();
            pass(description);
        } catch (Throwable error) {
            fail(description, "unexpected " + error.getClass().getSimpleName()
                    + ": " + error.getMessage());
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void pass(String description) {
        checks++;
        System.out.println("[PASS] " + description);
    }

    private static void fail(String description, String reason) {
        checks++;
        failures++;
        System.out.println("[FAIL] " + description + " (" + reason + ")");
    }

    @FunctionalInterface
    private interface Action {
        void run() throws Exception;
    }
}
