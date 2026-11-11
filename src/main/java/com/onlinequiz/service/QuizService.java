package com.onlinequiz.service;

import com.onlinequiz.exception.QuizNotFoundException;
import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.interfaces.QuizOperations;
import com.onlinequiz.model.Question;
import com.onlinequiz.model.Quiz;
import com.onlinequiz.repository.QuestionRepository;
import com.onlinequiz.repository.QuizRepository;
import com.onlinequiz.util.Validation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Quiz lifecycle operations. Quiz rows and their questions are loaded through
 * separate repositories and joined here, keeping each repository focused.
 */
public class QuizService implements QuizOperations {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    public QuizService() {
        this(new QuizRepository(), new QuestionRepository());
    }

    public QuizService(String databaseFile) {
        this(new QuizRepository(databaseFile), new QuestionRepository(databaseFile));
    }

    public QuizService(QuizRepository quizRepository, QuestionRepository questionRepository) {
        if (quizRepository == null || questionRepository == null) {
            throw new ValidationException("Quiz repositories are required");
        }
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    public boolean addQuiz(Quiz quiz) {
        if (quiz == null) {
            throw new ValidationException("Quiz is required");
        }
        try {
            quizRepository.createQuiz(quiz);
            return true;
        } catch (SQLException error) {
            throw new IllegalStateException("Could not create quiz", error);
        }
    }

    @Override
    public boolean updateQuiz(Quiz quiz) {
        if (quiz == null) {
            throw new ValidationException("Quiz is required");
        }
        Validation.requirePositive(quiz.getId(), "Quiz id");
        try {
            return quizRepository.updateQuiz(quiz);
        } catch (SQLException error) {
            throw new IllegalStateException("Could not update quiz " + quiz.getId(), error);
        }
    }

    @Override
    public boolean deleteQuiz(int quizId) {
        Validation.requirePositive(quizId, "Quiz id");
        try {
            return quizRepository.deleteQuiz(quizId);
        } catch (SQLException error) {
            throw new IllegalStateException("Could not delete quiz " + quizId, error);
        }
    }

    @Override
    public Quiz getQuizById(int quizId) throws QuizNotFoundException {
        Validation.requirePositive(quizId, "Quiz id");
        try {
            Quiz quiz = quizRepository.getQuizById(quizId);
            if (quiz == null) {
                throw new QuizNotFoundException("Quiz " + quizId + " was not found");
            }
            attachQuestions(quiz);
            return quiz;
        } catch (SQLException error) {
            throw new IllegalStateException("Could not load quiz " + quizId, error);
        }
    }

    /**
     * Returns every quiz with its questions loaded. The list is an ArrayList so
     * indexed access and iteration follow insertion order.
     */
    public List<Quiz> getAllQuizzes() {
        try {
            List<Quiz> quizzes = quizRepository.getAllQuizzes();
            for (Quiz quiz : quizzes) {
                attachQuestions(quiz);
            }
            return quizzes;
        } catch (SQLException error) {
            throw new IllegalStateException("Could not load quizzes", error);
        }
    }

    /**
     * Categories sorted alphabetically and without duplicates. TreeSet provides
     * both behaviours in a single collection.
     */
    public Set<String> getCategories() {
        Set<String> categories = new TreeSet<>();
        try {
            for (Quiz quiz : quizRepository.getAllQuizzes()) {
                categories.add(quiz.getCategory());
            }
            return categories;
        } catch (SQLException error) {
            throw new IllegalStateException("Could not load categories", error);
        }
    }

    /**
     * Questions are stored in an ArrayList by the repository; attaching them also
     * refreshes the cached question count.
     */
    private void attachQuestions(Quiz quiz) throws SQLException {
        List<Question> questions = questionRepository.getQuestionsByQuizId(quiz.getId());
        quiz.setQuestions(new ArrayList<>(questions));
    }
}
