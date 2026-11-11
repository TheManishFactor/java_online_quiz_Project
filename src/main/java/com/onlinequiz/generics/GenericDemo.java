package com.onlinequiz.generics;

import com.onlinequiz.model.Question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Executable demonstration of generic classes, generic methods, two type
 * parameters and bounded type parameters, connected to quiz domain objects.
 *
 * Run with:
 * java -cp "target/classes" com.onlinequiz.generics.GenericDemo
 */
public final class GenericDemo {

    private GenericDemo() {
    }

    public static void main(String[] args) {
        demonstrateGenericClass();
        demonstrateTwoTypeParameters();
        demonstrateGenericMethods();
        demonstrateBoundedTypeParameter();
        System.out.println();
        System.out.println("Generic type safety: the compiler rejects a Student in a"
                + " GenericRepository<Question> before the program runs.");
    }

    private static void demonstrateGenericClass() {
        System.out.println("1. GENERIC CLASS WITH ONE TYPE PARAMETER");
        GenericRepository<Question> questionCache = new GenericRepository<>();
        questionCache.add(question(101, "Which keyword inherits a class?"));
        questionCache.add(question(102, "Which collection keeps insertion order?"));
        questionCache.add(question(103, "Which method starts a thread?"));

        System.out.println("   cached questions : " + questionCache.size());
        System.out.println("   lookup id 102    : " + questionCache.getById(102).getQuestionText());
        System.out.println("   remove id 103    : " + questionCache.remove(103));
        System.out.println("   size after remove: " + questionCache.size());

        // The same class works for another stored type without any cast.
        GenericRepository<Question> secondCache = new GenericRepository<>();
        secondCache.add(question(201, "Can one generic class hold many types?"));
        System.out.println("   second cache size: " + secondCache.size());
    }

    private static void demonstrateTwoTypeParameters() {
        System.out.println();
        System.out.println("2. GENERIC CLASS WITH TWO TYPE PARAMETERS");
        GenericPair<Integer, String> student = new GenericPair<>(7, "Asha Rao");
        System.out.println("   pair      : " + student);
        System.out.println("   first id  : " + student.getFirst());
        System.out.println("   second    : " + student.getSecond());
        System.out.println("   swapped   : " + student.swap());

        GenericPair<String, Double> topScore = GenericPair.of("Java Basics", 92.5);
        System.out.println("   top score : " + topScore);
    }

    private static void demonstrateGenericMethods() {
        System.out.println();
        System.out.println("3. GENERIC METHODS");
        List<Question> questions = new ArrayList<>(Arrays.asList(
                question(1, "What is polymorphism?"),
                question(2, "What is a thread?"),
                question(3, "What is JDBC?")));

        GenericUtils.display("   all questions:", questions);

        Question found = GenericUtils.find(questions,
                question -> question.getQuestionText().contains("thread"));
        System.out.println("   find 'thread' : " + (found == null ? "none" : found.getQuestionText()));

        int longQuestions = GenericUtils.count(questions,
                question -> question.getQuestionText().length() > 18);
        System.out.println("   count >18 chars: " + longQuestions);
    }

    private static void demonstrateBoundedTypeParameter() {
        System.out.println();
        System.out.println("4. BOUNDED TYPE PARAMETER");
        List<Integer> scores = Arrays.asList(45, 88, 92, 67);
        List<String> categories = Arrays.asList("Programming", "General Knowledge", "Java");
        System.out.println("   max score      : " + GenericUtils.max(scores));
        System.out.println("   max category   : " + GenericUtils.max(categories));
        System.out.println("   Comparable<T>  : bounds both calls to types with compareTo()");
    }

    private static Question question(int id, String text) {
        return new Question(id, 1, text, "Option A", "Option B", "Option C", "Option D", "A");
    }
}
