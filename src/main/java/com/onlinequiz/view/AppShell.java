package com.onlinequiz.view;

import com.onlinequiz.model.User;
import com.onlinequiz.navigation.NavigationManager;
import com.onlinequiz.navigation.ScreenArea;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Shared dashboard frame: brand, signed-in account and role-aware navigation on
 * the left, screen content on the right. Views only provide the content.
 */
public final class AppShell extends BorderPane {

    private final StackPane contentHost = new StackPane();

    public AppShell(User user, NavigationManager navigation, ScreenArea active) {
        getStyleClass().add("app-shell");

        contentHost.getStyleClass().add("content-host");
        contentHost.setPadding(new Insets(24, 30, 24, 30));
        setCenter(contentHost);
        setLeft(createSidebar(user, navigation, active));
    }

    public void setContent(Node content) {
        contentHost.getChildren().setAll(content);
    }

    private VBox createSidebar(User user, NavigationManager navigation, ScreenArea active) {
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(252);
        sidebar.setMinWidth(252);
        sidebar.setPadding(new Insets(22, 16, 18, 16));

        HBox brand = new HBox(11);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.getStyleClass().add("brand-row");
        Label logo = new Label("Q");
        logo.getStyleClass().add("brand-logo");
        VBox brandText = new VBox(1,
                styledLabel("QUIZ STUDIO", "brand-title"),
                styledLabel("Advanced Java", "brand-subtitle"));
        brand.getChildren().addAll(logo, brandText);

        HBox account = new HBox(10);
        account.setAlignment(Pos.CENTER_LEFT);
        account.getStyleClass().add("account-card");
        Label avatar = new Label(user.getInitials());
        avatar.getStyleClass().add("avatar");
        Label name = styledLabel(user.getName(), "account-name");
        name.setMaxWidth(140);
        Label role = styledLabel(user.isAdmin() ? "Administrator" : "Student", "account-role");
        VBox accountText = new VBox(2, name, role);
        accountText.setMinWidth(0);
        HBox.setHgrow(accountText, Priority.ALWAYS);
        account.getChildren().addAll(avatar, accountText);

        VBox menu = new VBox(4);
        menu.getStyleClass().add("nav-menu");
        Label menuLabel = styledLabel("WORKSPACE", "nav-section");
        menu.getChildren().add(menuLabel);

        if (user.isAdmin()) {
            menu.getChildren().addAll(
                    navButton("⌂", "Dashboard", active == ScreenArea.ADMIN_DASHBOARD,
                            navigation::showAdminDashboard),
                    navButton("▤", "Manage Quizzes", active == ScreenArea.QUIZ_MANAGEMENT,
                            navigation::showQuizManagement),
                    navButton("✎", "Manage Questions", active == ScreenArea.QUESTION_MANAGEMENT,
                            navigation::showQuestionManagement),
                    navButton("◉", "Manage Students", active == ScreenArea.STUDENT_MANAGEMENT,
                            navigation::showStudentManagement),
                    navButton("◔", "Results & Analytics", active == ScreenArea.ANALYTICS,
                            () -> navigation.showAnalytics(0)));
        } else {
            menu.getChildren().addAll(
                    navButton("⌂", "Dashboard", active == ScreenArea.STUDENT_DASHBOARD,
                            navigation::showStudentDashboard),
                    navButton("▤", "Available Quizzes", active == ScreenArea.QUIZ_SELECTION,
                            navigation::showQuizSelection),
                    navButton("◷", "My Results", active == ScreenArea.HISTORY,
                            navigation::showHistory));
        }

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Region divider = new Region();
        divider.getStyleClass().add("sidebar-divider");
        Label footer = styledLabel("Online Quiz Application", "sidebar-footer");
        Button logout = UiFactory.ghostButton("↪   Sign out");
        logout.getStyleClass().add("sidebar-logout");
        logout.setAlignment(Pos.CENTER_LEFT);
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setOnAction(event -> navigation.logout());

        sidebar.getChildren().addAll(brand, account, menu, spacer, divider, footer, logout);
        return sidebar;
    }

    private Button navButton(String icon, String text, boolean active, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setGraphic(UiFactory.icon(icon));
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        if (active) {
            button.getStyleClass().add("nav-active");
        }
        button.setOnAction(event -> action.run());
        return button;
    }

    private Label styledLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }
}
