package com.onlinequiz.service;

import com.onlinequiz.exception.QuizNotFoundException;
import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.model.Result;
import com.onlinequiz.model.User;
import com.onlinequiz.repository.QuizRepository;
import com.onlinequiz.repository.ResultRepository;
import com.onlinequiz.repository.UserRepository;
import com.onlinequiz.util.Validation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Persists and retrieves quiz attempts. A result can only be saved for an
 * existing student and an existing quiz, which keeps the foreign keys valid and
 * produces a useful error before SQLite does.
 */
public class ResultService {

    private final ResultRepository resultRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;

    public ResultService() {
        this(new ResultRepository(), new UserRepository(), new QuizRepository());
    }

    public ResultService(String databaseFile) {
        this(new ResultRepository(databaseFile), new UserRepository(databaseFile),
                new QuizRepository(databaseFile));
    }

    public ResultService(ResultRepository resultRepository,
                         UserRepository userRepository,
                         QuizRepository quizRepository) {
        if (resultRepository == null || userRepository == null || quizRepository == null) {
            throw new ValidationException("Result repositories are required");
        }
        this.resultRepository = resultRepository;
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
    }

    public Result saveResult(Result result) throws QuizNotFoundException {
        if (result == null) {
            throw new ValidationException("Result is required");
        }
        try {
            User student = userRepository.findUserById(result.getUserId());
            if (student == null) {
                throw new ValidationException(
                        "Student " + result.getUserId() + " does not exist");
            }
            if (student.isAdmin()) {
                throw new ValidationException("Results can only be saved for students");
            }
            if (quizRepository.getQuizById(result.getQuizId()) == null) {
                throw new QuizNotFoundException(
                        "Quiz " + result.getQuizId() + " was not found");
            }
            return resultRepository.saveResult(result);
        } catch (SQLException error) {
            throw new IllegalStateException("Could not save result", error);
        }
    }

    public Result getResultById(int resultId) {
        Validation.requirePositive(resultId, "Result id");
        try {
            return resultRepository.getResultById(resultId);
        } catch (SQLException error) {
            throw new IllegalStateException("Could not load result " + resultId, error);
        }
    }

    public List<Result> getResultsByStudentId(int studentId) {
        Validation.requirePositive(studentId, "Student id");
        try {
            return resultRepository.getResultsByStudentId(studentId);
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "Could not load results for student " + studentId, error);
        }
    }

    public List<Result> getResultsByQuizId(int quizId) {
        Validation.requirePositive(quizId, "Quiz id");
        try {
            return resultRepository.getResultsByQuizId(quizId);
        } catch (SQLException error) {
            throw new IllegalStateException(
                    "Could not load results for quiz " + quizId, error);
        }
    }

    public List<Result> getAllResults() {
        try {
            return resultRepository.getAllResults();
        } catch (SQLException error) {
            throw new IllegalStateException("Could not load results", error);
        }
    }

    /**
     * Groups attempts by student id with a HashMap and traverses the flat result
     * list with an explicit Iterator.
     */
    public Map<Integer, List<Result>> groupResultsByStudent() {
        Map<Integer, List<Result>> grouped = new HashMap<>();
        Iterator<Result> iterator = getAllResults().iterator();
        while (iterator.hasNext()) {
            Result result = iterator.next();
            grouped.computeIfAbsent(result.getUserId(), key -> new ArrayList<>())
                    .add(result);
        }
        return grouped;
    }
}
