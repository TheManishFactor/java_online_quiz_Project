package com.onlinequiz.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Dashboard statistic tile: small icon, caption, large value and an optional
 * supporting line. The value can be refreshed after background data loading.
 */
public final class StatCard extends VBox {

    private final Label captionLabel;
    private final Label valueLabel;
    private final Label descriptionLabel;
    private final Label iconLabel;

    public StatCard(String caption, String initialValue) {
        this(null, caption, initialValue, null, "accent-indigo");
    }

    public StatCard(String icon, String caption, String initialValue,
                    String description, String accentClass) {
        getStyleClass().add("stat-card");
        setSpacing(8);
        setMinWidth(0);
        setMaxWidth(Double.MAX_VALUE);

        iconLabel = new Label(icon == null ? "" : icon);
        iconLabel.getStyleClass().addAll("stat-icon",
                accentClass == null ? "accent-indigo" : accentClass);
        iconLabel.setVisible(icon != null);
        iconLabel.setManaged(icon != null);

        captionLabel = new Label(caption);
        captionLabel.getStyleClass().add("stat-caption");
        captionLabel.setWrapText(true);

        HBox top = new HBox(10, iconLabel, captionLabel);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setMinWidth(0);

        valueLabel = new Label(initialValue);
        valueLabel.getStyleClass().add("stat-value");

        descriptionLabel = new Label(description == null ? "" : description);
        descriptionLabel.getStyleClass().add("stat-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setVisible(description != null);
        descriptionLabel.setManaged(description != null);

        getChildren().addAll(top, valueLabel, descriptionLabel);
    }

    public void setValue(String value) {
        valueLabel.setText(value == null ? "-" : value);
    }

    public void setCaption(String caption) {
        captionLabel.setText(caption == null ? "" : caption);
    }

    public void setDescription(String description) {
        descriptionLabel.setText(description == null ? "" : description);
        descriptionLabel.setVisible(description != null);
        descriptionLabel.setManaged(description != null);
    }
}
