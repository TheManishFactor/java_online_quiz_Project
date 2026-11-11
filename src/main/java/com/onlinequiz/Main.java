package com.onlinequiz;

import com.onlinequiz.database.DatabaseInitializer;
import com.onlinequiz.navigation.NavigationManager;
import com.onlinequiz.service.QuestionService;
import com.onlinequiz.service.QuizService;
import com.onlinequiz.service.ResultService;
import com.onlinequiz.service.UserService;
import com.onlinequiz.util.ExceptionHandler;
import com.onlinequiz.view.UiFactory;
import javafx.application.Application;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Desktop entry point. Initializes the SQLite schema, builds the service layer once
 * and hands control to the navigation manager, which owns the scene and session.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            DatabaseInitializer.initialize();
        } catch (SQLException error) {
            ExceptionHandler.handle(error, "Database startup");
            UiFactory.errorAlert(stage, "Database error",
                    "The quiz database could not be initialized. Please check the"
                            + " application folder permissions and try again.");
            return;
        }

        UserService userService = new UserService();
        QuizService quizService = new QuizService();
        QuestionService questionService = new QuestionService();
        ResultService resultService = new ResultService();

        NavigationManager navigation = new NavigationManager(stage, userService,
                quizService, questionService, resultService);
        navigation.showWelcome();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
