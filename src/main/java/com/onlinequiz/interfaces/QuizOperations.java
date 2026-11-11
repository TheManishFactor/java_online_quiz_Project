package com.onlinequiz.interfaces;

import com.onlinequiz.exception.QuizNotFoundException;
import com.onlinequiz.model.Quiz;

/**
 * Contract for quiz persistence and management operations. The JDBC-backed
 * implementation is {@code com.onlinequiz.service.QuizService}.
 */
public interface QuizOperations {

    boolean addQuiz(Quiz quiz);

    boolean updateQuiz(Quiz quiz);

    boolean deleteQuiz(int quizId);

    Quiz getQuizById(int quizId) throws QuizNotFoundException;
}
