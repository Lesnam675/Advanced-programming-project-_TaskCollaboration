package com.aastu.taskmanagersystem.client.ui.styles;

import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Fits stages to the primary monitor visual bounds (~85%), centered, without exceeding the screen.
 */
public final class UiWindowHelper {

    private static final double SCREEN_FRACTION = 0.85;

    private UiWindowHelper() {}

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    /**
     * @param maxPreferredWidth  optional cap (e.g. login dialog); null = use 85% width only
     * @param maxPreferredHeight optional cap (e.g. login dialog); null = use 85% height only
     */
    public static Scene configureStage(
            Stage stage,
            Parent root,
            double minWidth,
            double minHeight,
            Double maxPreferredWidth,
            Double maxPreferredHeight
    ) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double screenW = bounds.getWidth();
        double screenH = bounds.getHeight();

        double width = screenW * SCREEN_FRACTION;
        double height = screenH * SCREEN_FRACTION;

        if (maxPreferredWidth != null) {
            width = Math.min(width, maxPreferredWidth);
        }
        if (maxPreferredHeight != null) {
            height = Math.min(height, maxPreferredHeight);
        }

        double effectiveMinW = Math.min(minWidth, screenW);
        double effectiveMinH = Math.min(minHeight, screenH);

        width = clamp(width, effectiveMinW, screenW);
        height = clamp(height, effectiveMinH, screenH);

        Scene scene = new Scene(root, width, height);

        stage.setMinWidth(effectiveMinW);
        stage.setMinHeight(effectiveMinH);
        stage.setScene(scene);
        stage.setWidth(width);
        stage.setHeight(height);
        stage.setX(bounds.getMinX() + (screenW - width) / 2);
        stage.setY(bounds.getMinY() + (screenH - height) / 2);

        return scene;
    }

    public static Scene configureMainStage(Stage stage, Parent root) {
        return configureStage(stage, root, 1000, 650, null, null);
    }

    public static Scene configureLoginStage(Stage stage, Parent root) {
        return configureStage(stage, root, 420, 480, 480.0, 520.0);
    }
}
