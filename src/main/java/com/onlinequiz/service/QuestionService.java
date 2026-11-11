package com.onlinequiz.service;

import com.onlinequiz.exception.InvalidQuestionException;
import com.onlinequiz.exception.QuizNotFoundException;
import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.model.Question;
import com.onlinequiz.repository.QuestionRepository;
import com.onlinequiz.repository.QuizRepository;
import com.onlinequiz.util.Validation;

import java.sql.SQLException;
import java.util.List;

/**
 * Question management. Every write validates that the owning quiz exists before
 * the question repository is called.
 */
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    public QuestionService() {
        this(new QuestionRepository(), new QuizRepository());
    }

    public QuestionService(String databaseFile) {
        this(new QuestionRepository(databaseFile), new QuizRepository(databaseFile));
    }

    public QuestionService(QuestionRepository questionRepository,
                           QuizRepository quizRepository) {
        if (questionRepository == null || quizRepository == null) {
            throw new ValidationException("Question repositories are required");
        }
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
    }

    public Question addQuestion(Question question) throws QuizNotFoundException {
        if (question == null) {
            throw new ValidationException("Question is required");
        }
        try {
            if (quizRepository.getQuizById(question.getQuizId()) == null) {
                throw new QuizNotFoundException(
                        "Quiz " + question.getQuizId() + " was not found");
            }
            questionRepository.addQuestion(question);
            return question;
        } catch (SQLException error) {
            throw new IllegalStateException("Could not add question", error);
        }
    }

    public List<Question> getQuestionsByQuizId(int quizId) throws QuizNotFoundException {
        Validation.requirePositive(quizId, "Quiz id");
        try {
            if (quizRepository.getQuizById(quizId) == null) {
                throw new QuizNotFoundException("Quiz " + quizId + " was not found");
            }
            return questionRepository.getQuestionsByQuizId(quizId);
        } catch (SQLException error) {
            throw new IllegalStateException("Could not load questions for quiz " + quizId, error);
        }
    }

    /**
     * Unchecked exception: a missing question is a data-integrity problem for the
     * caller that supplied the id.
     */
    public Question getQuestionById(int questionId) {
        Validation.requirePositive(questionId, "Question id");
        try {
            Question question = questionRepository.getQuestionById(questionId);
            if (question == null) {
                throw new InvalidQuestionException(
                        "Question " + questionId + " was not found");
            }
            return question;
        } catch (SQLException error) {
            throw new IllegalStateException("Could not load question " + questionId, error);
        }
    }

    public boolean updateQuestion(Question question) {
        if (question == null) {
            throw new ValidationException("Question is required");
        }
        Validation.requirePositive(question.getId(), "Question id");
        try {
            return questionRepository.updateQuestion(question);
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "Could not update question " + question.getId(), error);
        }
    }

    public boolean deleteQuestion(int questionId) {
        Validation.requirePositive(questionId, "Question id");
        try {
            return questionRepository.deleteQuestion(questionId);
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "Could not delete question " + questionId, error);
        }
    }
}
