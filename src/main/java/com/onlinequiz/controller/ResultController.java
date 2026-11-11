package com.onlinequiz.controller;

import com.onlinequiz.model.Question;
import com.onlinequiz.model.Quiz;
import com.onlinequiz.model.Result;
import com.onlinequiz.model.User;
import com.onlinequiz.navigation.NavigationManager;
import com.onlinequiz.view.ResultView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Presents one graded attempt: the summary, the answer review and the two
 * follow-up actions. The view is already final by the time this controller runs,
 * so exposing the correct answers here cannot leak them before submission.
 */
public final class ResultController {

    public ResultController(NavigationManager navigation, User student, Quiz quiz,
                            Result result, String[] answers, long seconds,
                            ResultView view) {
        view.setSummary(student.getName(), quiz.getTitle(), result, seconds);
        view.setReview(buildReview(quiz, answers));

        view.getRetakeButton().setOnAction(event -> navigation.showQuizAttempt(quiz));
        view.getBackButton().setOnAction(event -> navigation.showStudentDashboard());
    }

    private List<ResultView.ReviewItem> buildReview(Quiz quiz, String[] answers) {
        List<ResultView.ReviewItem> items = new ArrayList<>();
        List<Question> questions = quiz.getQuestions();
        for (int index = 0; index < questions.size(); index++) {
            Question question = questions.get(index);
            String selected = answers != null && index < answers.length ? answers[index] : null;
            items.add(new ResultView.ReviewItem(
                    question.getQuestionText(),
                    describe(question, selected),
                    describe(question, question.getCorrectAnswer()),
                    question.isCorrect(selected)));
        }
        return items;
    }

    /**
     * Renders a stored answer label as "B. Option text", or a clear marker when the
     * student skipped the question.
     */
    private String describe(Question question, String label) {
        if (label == null || label.trim().isEmpty()) {
            return "Not answered";
        }
        String normalized = label.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() != 1 || "ABCD".indexOf(normalized.charAt(0)) < 0) {
            return "Not answered";
        }
        return normalized + ". " + question.getOption(normalized.charAt(0));
    }
}
