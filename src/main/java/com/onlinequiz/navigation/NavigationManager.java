package com.onlinequiz.navigation;

import com.onlinequiz.controller.AdminDashboardController;
import com.onlinequiz.controller.AdminSetupController;
import com.onlinequiz.controller.AnalyticsController;
import com.onlinequiz.controller.HistoryController;
import com.onlinequiz.controller.LoginController;
import com.onlinequiz.controller.QuestionManagementController;
import com.onlinequiz.controller.QuizAttemptController;
import com.onlinequiz.controller.QuizManagementController;
import com.onlinequiz.controller.QuizSelectionController;
import com.onlinequiz.controller.RegisterController;
import com.onlinequiz.controller.ResultController;
import com.onlinequiz.controller.StudentDashboardController;
import com.onlinequiz.controller.StudentManagementController;
import com.onlinequiz.controller.WelcomeController;
import com.onlinequiz.exception.QuizNotFoundException;
import com.onlinequiz.model.Quiz;
import com.onlinequiz.model.Result;
import com.onlinequiz.model.User;
import com.onlinequiz.service.QuestionService;
import com.onlinequiz.service.QuizService;
import com.onlinequiz.service.ResultService;
import com.onlinequiz.service.UserService;
import com.onlinequiz.view.AdminDashboardView;
import com.onlinequiz.view.AdminSetupView;
import com.onlinequiz.view.AnalyticsView;
import com.onlinequiz.view.AppShell;
import com.onlinequiz.view.HistoryView;
import com.onlinequiz.view.LoginView;
import com.onlinequiz.view.QuestionManagementView;
import com.onlinequiz.view.QuizAttemptView;
import com.onlinequiz.view.QuizManagementView;
import com.onlinequiz.view.QuizSelectionView;
import com.onlinequiz.view.RegisterView;
import com.onlinequiz.view.ResultView;
import com.onlinequiz.view.StudentDashboardView;
import com.onlinequiz.view.StudentManagementView;
import com.onlinequiz.view.UiFactory;
import com.onlinequiz.view.WelcomeView;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Central screen router and session owner.
 *
 * Every screen change goes through this class so that four things always happen
 * together: the role policy is checked, the previous screen's lifecycle is ended
 * (which stops its timer or background updates), an authenticated session is kept
 * in one place, and the new screen is installed with the shared theme.
 */
public final class NavigationManager {

    private static final double WINDOW_WIDTH = 1180;
    private static final double WINDOW_HEIGHT = 760;
    private static final double MIN_WIDTH = 1024;
    private static final double MIN_HEIGHT = 680;

    private final Stage stage;
    private final Scene scene;
    private final UserService userService;
    private final QuizService quizService;
    private final QuestionService questionService;
    private final ResultService resultService;

    private User currentUser;
    private ScreenArea currentArea;
    private ScreenLifecycle currentLifecycle;

    public NavigationManager(Stage stage, UserService userService, QuizService quizService,
                             QuestionService questionService, ResultService resultService) {
        if (stage == null) {
            throw new IllegalArgumentException("A JavaFX stage is required");
        }
        this.stage = stage;
        this.userService = userService;
        this.quizService = quizService;
        this.questionService = questionService;
        this.resultService = resultService;

        this.scene = new Scene(new StackPane(), WINDOW_WIDTH, WINDOW_HEIGHT);
        URL theme = NavigationManager.class.getResource(UiFactory.THEME_RESOURCE);
        if (theme != null) {
            scene.getStylesheets().add(theme.toExternalForm());
        } else {
            System.err.println("Theme stylesheet not found: " + UiFactory.THEME_RESOURCE);
        }

        stage.setScene(scene);
        stage.setTitle("Online Quiz Application");
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
    }

    public Stage getStage() {
        return stage;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public ScreenArea getCurrentArea() {
        return currentArea;
    }

    // ------------------------------------------------------------------ public

    public void showWelcome() {
        if (!allow(ScreenArea.WELCOME)) {
            return;
        }
        WelcomeView view = new WelcomeView();
        new WelcomeController(this, userService, view);
        replaceRoot(view, null, ScreenArea.WELCOME);
    }

    public void showLogin() {
        showLogin(null, null);
    }

    public void showLogin(String email, String message) {
        if (!allow(ScreenArea.LOGIN)) {
            return;
        }
        LoginView view = new LoginView();
        new LoginController(this, userService, view);
        if (email != null) {
            view.setEmail(email);
        }
        if (message != null) {
            view.setSuccess(message);
        }
        replaceRoot(view, null, ScreenArea.LOGIN);
    }

    public void showRegister() {
        if (!allow(ScreenArea.REGISTER)) {
            return;
        }
        RegisterView view = new RegisterView();
        new RegisterController(this, userService, view);
        replaceRoot(view, null, ScreenArea.REGISTER);
    }

    public void showAdminSetup() {
        if (!allow(ScreenArea.ADMIN_SETUP)) {
            return;
        }
        if (userService.hasAdmin()) {
            showLogin(null, "An administrator account already exists. Sign in instead.");
            return;
        }
        AdminSetupView view = new AdminSetupView();
        new AdminSetupController(this, userService, view);
        replaceRoot(view, null, ScreenArea.ADMIN_SETUP);
    }

    /**
     * Stores the authenticated user and routes by role.
     */
    public void loginSucceeded(User user) {
        if (user == null) {
            showLogin(null, "Sign in to continue.");
            return;
        }
        currentUser = user;
        if (user.isAdmin()) {
            showAdminDashboard();
        } else {
            showStudentDashboard();
        }
    }

    public void logout() {
        if (currentUser != null) {
            userService.logoutUser(currentUser.getId());
        }
        currentUser = null;
        showWelcome();
    }

    // ---------------------------------------------------------------- student

    public void showStudentDashboard() {
        if (!allow(ScreenArea.STUDENT_DASHBOARD)) {
            return;
        }
        StudentDashboardView view = new StudentDashboardView();
        StudentDashboardController controller = new StudentDashboardController(
                this, quizService, resultService, currentUser, view);
        showAppScreen(ScreenArea.STUDENT_DASHBOARD, UiFactory.scroll(view), controller);
    }

    public void showQuizSelection() {
        if (!allow(ScreenArea.QUIZ_SELECTION)) {
            return;
        }
        QuizSelectionView view = new QuizSelectionView();
        QuizSelectionController controller = new QuizSelectionController(
                this, quizService, view);
        showAppScreen(ScreenArea.QUIZ_SELECTION, view, controller);
    }

    /**
     * Opens a fresh attempt. The quiz is reloaded by id so the attempt always uses
     * the questions currently stored in SQLite, even for a retake.
     */
    public void showQuizAttempt(Quiz quiz) {
        if (!allow(ScreenArea.QUIZ_ATTEMPT)) {
            return;
        }
        if (quiz == null) {
            showQuizSelection();
            return;
        }
        Quiz prepared;
        try {
            prepared = quizService.getQuizById(quiz.getId());
        } catch (QuizNotFoundException error) {
            showError("Quiz unavailable", "This quiz no longer exists.");
            showQuizSelection();
            return;
        }
        if (!prepared.hasQuestions()) {
            showError("Quiz unavailable", "This quiz has no questions yet.");
            showQuizSelection();
            return;
        }

        QuizAttemptView view = new QuizAttemptView();
        QuizAttemptController controller = new QuizAttemptController(
                this, resultService, currentUser, prepared, view);
        showAppScreen(ScreenArea.QUIZ_ATTEMPT, view, controller);
    }

    public void showResult(Quiz quiz, Result result, String[] answers, long seconds) {
        if (!allow(ScreenArea.RESULT)) {
            return;
        }
        if (quiz == null || result == null) {
            showStudentDashboard();
            return;
        }
        ResultView view = new ResultView();
        new ResultController(this, currentUser, quiz, result, answers, seconds, view);
        showAppScreen(ScreenArea.RESULT, UiFactory.scroll(view), null);
    }

    public void showHistory() {
        if (!allow(ScreenArea.HISTORY)) {
            return;
        }
        HistoryView view = new HistoryView();
        HistoryController controller = new HistoryController(
                this, resultService, quizService, currentUser, view);
        showAppScreen(ScreenArea.HISTORY, view, controller);
    }

    public void showProfileDialog() {
        if (currentUser == null) {
            return;
        }
        Label avatar = new Label(currentUser.getInitials());
        avatar.getStyleClass().add("avatar");
        VBox identity = new VBox(2,
                styled(currentUser.getName(), "account-name"),
                styled(currentUser.isAdmin() ? "Administrator" : "Student", "account-role"));
        HBox header = new HBox(12, avatar, identity);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox details = new VBox(10,
                detailRow("Full name", currentUser.getName()),
                detailRow("Email", currentUser.getEmail()),
                detailRow("Role", currentUser.getRole()),
                detailRow("Account ID", String.valueOf(currentUser.getId())));

        VBox content = UiFactory.card(header, UiFactory.separator(), details,
                UiFactory.muted("Passwords are stored only as PBKDF2 hashes and are"
                        + " never displayed."));
        content.setPrefWidth(400);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("My profile");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setContent(content);
        UiFactory.styleDialog(dialog);
        dialog.initOwner(stage);
        dialog.showAndWait();
    }

    // ------------------------------------------------------------------ admin

    public void showAdminDashboard() {
        if (!allow(ScreenArea.ADMIN_DASHBOARD)) {
            return;
        }
        AdminDashboardView view = new AdminDashboardView();
        AdminDashboardController controller = new AdminDashboardController(
                this, userService, quizService, resultService, currentUser, view);
        showAppScreen(ScreenArea.ADMIN_DASHBOARD, UiFactory.scroll(view), controller);
    }

    public void showQuizManagement() {
        if (!allow(ScreenArea.QUIZ_MANAGEMENT)) {
            return;
        }
        QuizManagementView view = new QuizManagementView();
        QuizManagementController controller = new QuizManagementController(
                this, quizService, view);
        showAppScreen(ScreenArea.QUIZ_MANAGEMENT, view, controller);
    }

    public void showQuestionManagement() {
        if (!allow(ScreenArea.QUESTION_MANAGEMENT)) {
            return;
        }
        QuestionManagementView view = new QuestionManagementView();
        QuestionManagementController controller = new QuestionManagementController(
                this, quizService, questionService, view);
        showAppScreen(ScreenArea.QUESTION_MANAGEMENT, view, controller);
    }

    public void showStudentManagement() {
        if (!allow(ScreenArea.STUDENT_MANAGEMENT)) {
            return;
        }
        StudentManagementView view = new StudentManagementView();
        StudentManagementController controller = new StudentManagementController(
                this, userService, resultService, quizService, view);
        showAppScreen(ScreenArea.STUDENT_MANAGEMENT, view, controller);
    }

    public void showAnalytics(int initialTab) {
        if (!allow(ScreenArea.ANALYTICS)) {
            return;
        }
        AnalyticsView view = new AnalyticsView();
        AnalyticsController controller = new AnalyticsController(
                userService, quizService, resultService, view, initialTab);
        showAppScreen(ScreenArea.ANALYTICS, view, controller);
    }

    // --------------------------------------------------------------- internals

    public void showError(String title, String content) {
        UiFactory.errorAlert(stage, title, content);
    }

    /**
     * Role guard. A denied request is redirected to the correct dashboard instead
     * of showing a forbidden screen.
     */
    private boolean allow(ScreenArea area) {
        if (AccessPolicy.canAccess(currentUser, area)) {
            return true;
        }
        if (AccessPolicy.isAdmin(currentUser)) {
            showAdminDashboard();
        } else if (AccessPolicy.isStudent(currentUser)) {
            showStudentDashboard();
        } else {
            showWelcome();
        }
        return false;
    }

    private void showAppScreen(ScreenArea area, Node content, ScreenLifecycle lifecycle) {
        AppShell shell = new AppShell(currentUser, this, area);
        shell.setContent(content);
        replaceRoot(shell, lifecycle, area);
    }

    private void replaceRoot(Parent root, ScreenLifecycle lifecycle, ScreenArea area) {
        ScreenLifecycle previous = currentLifecycle;
        currentLifecycle = null;
        if (previous != null) {
            previous.onHide();
        }
        currentArea = area;
        currentLifecycle = lifecycle;
        scene.setRoot(root);
        UiFactory.fadeIn(root);
    }

    private HBox detailRow(String caption, String value) {
        Label captionLabel = new Label(caption);
        captionLabel.getStyleClass().add("metric-caption");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("metric-value");
        HBox row = new HBox(12, captionLabel, UiFactory.spacer(), valueLabel);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(row, Priority.ALWAYS);
        return row;
    }

    private Label styled(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }
}
