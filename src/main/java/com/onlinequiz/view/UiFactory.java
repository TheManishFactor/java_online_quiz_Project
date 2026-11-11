package com.onlinequiz.view;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Duration;

import java.net.URL;
import java.util.Optional;

/**
 * Reusable JavaFX controls and layout helpers. Keeping styling and common
 * structures here makes every screen consistent and avoids duplicated CSS class
 * names in the controllers.
 */
public final class UiFactory {

    public static final String THEME_RESOURCE = "/css/theme.css";

    private UiFactory() {
    }

    public static Button button(String text, String... styleClasses) {
        Button button = new Button(text);
        button.getStyleClass().addAll(styleClasses);
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    public static Button primaryButton(String text) {
        return button(text, "btn-primary");
    }

    public static Button secondaryButton(String text) {
        return button(text, "btn-secondary");
    }

    public static Button dangerButton(String text) {
        return button(text, "btn-danger");
    }

    public static Button ghostButton(String text) {
        return button(text, "btn-ghost");
    }

    public static Label pageTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("page-title");
        label.setWrapText(true);
        return label;
    }

    public static Label pageSubtitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("page-subtitle");
        label.setWrapText(true);
        return label;
    }

    public static Label sectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-title");
        return label;
    }

    public static Label muted(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-text");
        label.setWrapText(true);
        return label;
    }

    public static VBox pageHeader(String title, String subtitle) {
        VBox header = new VBox(4, pageTitle(title), pageSubtitle(subtitle));
        header.getStyleClass().add("page-header");
        return header;
    }

    public static VBox pageHeader(String eyebrow, String title, String subtitle) {
        Label eyebrowLabel = new Label(eyebrow);
        eyebrowLabel.getStyleClass().add("page-eyebrow");
        VBox header = new VBox(5, eyebrowLabel, pageTitle(title), pageSubtitle(subtitle));
        header.getStyleClass().add("page-header");
        return header;
    }

    public static VBox sectionHeader(String title, String subtitle) {
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("section-subtitle");
        subtitleLabel.setWrapText(true);
        VBox header = new VBox(3, sectionTitle(title), subtitleLabel);
        header.setMinWidth(0);
        return header;
    }

    /**
     * Small monochrome glyph used by sidebar navigation and list rows.
     */
    public static Label icon(String glyph) {
        Label label = new Label(glyph);
        label.getStyleClass().add("nav-icon");
        label.setMinWidth(18);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    /**
     * Equal-width responsive grid for dashboard stat tiles. Percentage columns
     * shrink with the window instead of forcing the page wider.
     */
    public static GridPane statGrid(Node... cards) {
        return columns(12, cards);
    }

    /**
     * Equal-width, percentage-based column grid. Cards keep a zero minimum width
     * so narrow windows shrink the layout instead of clipping it.
     */
    public static GridPane columns(double gap, Node... children) {
        GridPane grid = new GridPane();
        grid.setHgap(gap);
        grid.setVgap(gap);
        if (children.length == 0) {
            return grid;
        }
        RowConstraints row = new RowConstraints();
        row.setVgrow(Priority.ALWAYS);
        grid.getRowConstraints().add(row);
        double width = 100.0 / children.length;
        for (int index = 0; index < children.length; index++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(width);
            column.setMinWidth(0);
            column.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(column);

            Node child = children[index];
            grid.add(child, index, 0);
            fillWidth(child);
        }
        return grid;
    }

    /**
     * Two-panel dashboard grid where the left panel takes the larger share.
     */
    public static GridPane split(Node left, Node right) {
        return split(left, right, 58);
    }

    public static GridPane split(Node left, Node right, double leftPercent) {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        ColumnConstraints leftColumn = new ColumnConstraints();
        leftColumn.setPercentWidth(leftPercent);
        leftColumn.setMinWidth(0);
        leftColumn.setHgrow(Priority.ALWAYS);
        ColumnConstraints rightColumn = new ColumnConstraints();
        rightColumn.setPercentWidth(100 - leftPercent);
        rightColumn.setMinWidth(0);
        rightColumn.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(leftColumn, rightColumn);

        RowConstraints row = new RowConstraints();
        row.setVgrow(Priority.ALWAYS);
        grid.getRowConstraints().add(row);

        grid.add(left, 0, 0);
        grid.add(right, 1, 0);
        fillWidth(left);
        fillWidth(right);
        return grid;
    }

    /**
     * Lets a node expand inside its parent without imposing a minimum width.
     */
    public static void fillWidth(Node node) {
        if (node == null) {
            return;
        }
        GridPane.setHgrow(node, Priority.ALWAYS);
        GridPane.setVgrow(node, Priority.ALWAYS);
        if (node instanceof Region) {
            Region region = (Region) node;
            region.setMinWidth(0);
            region.setMaxWidth(Double.MAX_VALUE);
        }
    }

    public static VBox field(String labelText, Node input) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        VBox box = new VBox(6, label, input);
        box.getStyleClass().add("field-box");
        return box;
    }

    public static TextField textField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("app-field");
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    public static PasswordField passwordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.getStyleClass().add("app-field");
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    public static Label messageLabel() {
        Label label = new Label();
        label.getStyleClass().add("message-label");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setManaged(false);
        label.setVisible(false);
        return label;
    }

    public static void error(Label label, String message) {
        showMessage(label, message, "message-error");
    }

    public static void success(Label label, String message) {
        showMessage(label, message, "message-success");
    }

    public static void neutral(Label label, String message) {
        showMessage(label, message, "message-neutral");
    }

    public static void clear(Label label) {
        if (label == null) {
            return;
        }
        label.setText("");
        label.setManaged(false);
        label.setVisible(false);
    }

    private static void showMessage(Label label, String message, String styleClass) {
        if (label == null) {
            return;
        }
        label.getStyleClass().removeAll("message-error", "message-success", "message-neutral");
        label.getStyleClass().add(styleClass);
        label.setText(message == null ? "" : message);
        label.setManaged(true);
        label.setVisible(true);
    }

    public static VBox card(Node... children) {
        VBox box = new VBox(16);
        box.getStyleClass().add("card");
        box.getChildren().addAll(children);
        return box;
    }

    public static VBox emptyState(String message) {
        Label label = new Label(message);
        label.getStyleClass().add("empty-state");
        label.setWrapText(true);
        VBox box = new VBox(label);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(24));
        return box;
    }

    public static Separator separator() {
        Separator separator = new Separator();
        separator.getStyleClass().add("app-separator");
        return separator;
    }

    public static Label chip(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("chip");
        return label;
    }

    public static Label badge(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().addAll("badge", styleClass);
        return label;
    }

    public static ScrollPane scroll(Node content) {
        ScrollPane pane = new ScrollPane(content);
        pane.setFitToWidth(true);
        pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pane.getStyleClass().add("transparent-scroll");
        return pane;
    }

    public static Region spacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        VBox.setVgrow(region, Priority.ALWAYS);
        return region;
    }

    public static void fadeIn(Node node) {
        if (node == null) {
            return;
        }
        node.setOpacity(0.12);
        FadeTransition fade = new FadeTransition(Duration.millis(240), node);
        fade.setFromValue(0.12);
        fade.setToValue(1.0);
        fade.play();
    }

    /**
     * Adds a subtle scale lift on hover. Mouse enter/exit are JavaFX MouseEvents,
     * demonstrating event listeners on a plain Node as well as CSS :hover colours.
     */
    public static void addHoverLift(Node node) {
        if (node == null) {
            return;
        }
        ScaleTransition grow = new ScaleTransition(Duration.millis(130), node);
        grow.setToX(1.015);
        grow.setToY(1.015);
        ScaleTransition shrink = new ScaleTransition(Duration.millis(130), node);
        shrink.setToX(1.0);
        shrink.setToY(1.0);

        node.setOnMouseEntered(event -> {
            node.getStyleClass().add("hovered");
            grow.playFromStart();
        });
        node.setOnMouseExited(event -> {
            node.getStyleClass().remove("hovered");
            shrink.playFromStart();
        });
    }

    public static void styleDialog(Dialog<?> dialog) {
        if (dialog == null) {
            return;
        }
        URL theme = UiFactory.class.getResource(THEME_RESOURCE);
        if (theme != null) {
            dialog.getDialogPane().getStylesheets().add(theme.toExternalForm());
        }
        dialog.getDialogPane().getStyleClass().add("app-dialog");
    }

    public static boolean confirm(Window owner, String title, String header, String content) {
        ButtonType confirm = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, content, confirm, cancel);
        alert.setTitle(title);
        alert.setHeaderText(header);
        styleDialog(alert);
        if (owner != null) {
            alert.initOwner(owner);
        }
        Optional<ButtonType> answer = alert.showAndWait();
        return answer.isPresent() && answer.get() == confirm;
    }

    public static void errorAlert(Window owner, String title, String content) {
        showAlert(owner, Alert.AlertType.ERROR, title, content);
    }

    public static void infoAlert(Window owner, String title, String content) {
        showAlert(owner, Alert.AlertType.INFORMATION, title, content);
    }

    private static void showAlert(Window owner, Alert.AlertType type,
                                  String title, String content) {
        Alert alert = new Alert(type, content, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        styleDialog(alert);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }
}
