package com.onlinequiz.model;

import com.onlinequiz.exception.InvalidQuestionException;
import com.onlinequiz.exception.QuizNotFoundException;
import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.interfaces.Identifiable;
import com.onlinequiz.util.Validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Quiz aggregate. Questions are stored in an ArrayList so insertion order is kept
 * and indexed access is fast.
 */
public class Quiz implements Identifiable {

    public static final int MIN_DURATION_MINUTES = 1;

    private int id;
    private String title;
    private String category;
    private int duration;
    private int totalQuestions;
    private List<Question> questions;

    public Quiz(int id, String title, String category, int duration, int totalQuestions) {
        this.id = Validation.requireNonNegative(id, "Quiz id");
        this.title = Validation.requireText(title, "Quiz title");
        this.category = Validation.requireText(category, "Quiz category");
        setDuration(duration);
        setTotalQuestions(totalQuestions);
        this.questions = new ArrayList<>();
    }

    public Quiz(int id, String title, String category, int duration, List<Question> questions) {
        this(id, title, category, duration, 0);
        setQuestions(questions);
    }

    public Quiz(String title, String category, int duration) {
        this(0, title, category, duration, 0);
    }

    @Override
    public final int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = Validation.requireNonNegative(id, "Quiz id");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = Validation.requireText(title, "Quiz title");
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = Validation.requireText(category, "Quiz category");
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        if (duration < MIN_DURATION_MINUTES) {
            throw new ValidationException(
                    "Quiz duration must be at least " + MIN_DURATION_MINUTES + " minute");
        }
        this.duration = duration;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = Validation.requireNonNegative(totalQuestions, "Total questions");
    }

    public List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }

    public void setQuestions(List<Question> questions) {
        if (questions == null) {
            throw new ValidationException("Question list must not be null");
        }
        List<Question> copy = new ArrayList<>();
        for (Question question : questions) {
            if (question == null) {
                throw new ValidationException("Question list must not contain null values");
            }
            if (id > 0 && question.getQuizId() != id) {
                throw new ValidationException(
                        "Question " + question.getId() + " belongs to another quiz");
            }
            copy.add(question);
        }
        this.questions = copy;
        this.totalQuestions = copy.size();
    }

    /**
     * Adds a question and keeps the cached question count in sync.
     */
    public void addQuestion(Question question) {
        if (question == null) {
            throw new ValidationException("Question is required");
        }
        if (id > 0 && question.getQuizId() != id) {
            throw new ValidationException("Question does not belong to quiz " + id);
        }
        if (question.getId() > 0 && containsQuestionId(question.getId())) {
            throw new ValidationException(
                    "Question " + question.getId() + " is already part of this quiz");
        }
        questions.add(question);
        totalQuestions++;
    }

    /**
     * Removes the first question with the supplied id. Returns true when a question
     * was actually removed.
     */
    public boolean removeQuestion(int questionId) {
        for (int index = 0; index < questions.size(); index++) {
            if (questions.get(index).getId() == questionId) {
                questions.remove(index);
                totalQuestions = Math.max(0, totalQuestions - 1);
                return true;
            }
        }
        return false;
    }

    public Question getQuestion(int index) {
        if (index < 0 || index >= questions.size()) {
            throw new IndexOutOfBoundsException(
                    "Question index " + index + " is outside 0.." + (questions.size() - 1));
        }
        return questions.get(index);
    }

    /**
     * Looks up a stored question by its database id.
     *
     * Unchecked exception: a missing question inside an already loaded quiz is a
     * programming/integrity error, so {@link InvalidQuestionException} (a
     * {@link ValidationException}) does not force every caller to declare it.
     *
     * @throws InvalidQuestionException when no question with that id is in this quiz
     */
    public Question getQuestionById(int questionId) throws InvalidQuestionException {
        for (Question question : questions) {
            if (question.getId() == questionId) {
                return question;
            }
        }
        throw new InvalidQuestionException(
                "Question " + questionId + " was not found in quiz " + id);
    }

    /**
     * Finds a quiz by id in an in-memory collection.
     *
     * Checked exception: a missing quiz is a normal, recoverable situation for the
     * caller (for example showing "quiz not found" in the UI), so the compiler
     * forces the caller to decide how to handle it.
     *
     * @param quizzes collection to search; may be null
     * @param quizId  identifier to look for
     * @return the matching quiz
     * @throws QuizNotFoundException when the collection is null or the id is absent
     */
    public static Quiz findById(List<Quiz> quizzes, int quizId) throws QuizNotFoundException {
        if (quizzes == null) {
            throw new QuizNotFoundException("Quiz list is not available");
        }
        for (Quiz quiz : quizzes) {
            if (quiz != null && quiz.getId() == quizId) {
                return quiz;
            }
        }
        throw new QuizNotFoundException("Quiz " + quizId + " was not found");
    }

    public boolean hasQuestions() {
        return totalQuestions > 0;
    }

    public String getDurationLabel() {
        return duration + (duration == 1 ? " minute" : " minutes");
    }

    private boolean containsQuestionId(int questionId) {
        for (Question question : questions) {
            if (question.getId() == questionId) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Quiz{id=" + id + ", title='" + title + "', category='" + category
                + "', duration=" + duration + ", totalQuestions=" + totalQuestions + "}";
    }
}
