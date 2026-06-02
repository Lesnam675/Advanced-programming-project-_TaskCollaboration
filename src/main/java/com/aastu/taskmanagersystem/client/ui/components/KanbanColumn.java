package com.aastu.taskmanagersystem.client.ui.components;
import com.aastu.taskmanagersystem.client.ui.styles.UiStyles;
import com.aastu.taskmanagersystem.client.model.Task;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

/**
 * Trello/Jira-style column: header with count, scrollable task list, empty state.
 */
public class KanbanColumn {

    private final String title;
    private final String accentColor;
    private final VBox panel;
    private final VBox tasksList;
    private final Label headerLabel;
    private final Label emptyLabel;
    private final Circle statusDot;

    public KanbanColumn(String title, String accentColor) {
        this.title = title;
        this.accentColor = accentColor;

        statusDot = new Circle(5);
        statusDot.setStyle("-fx-fill: " + accentColor + ";");

        headerLabel = new Label(title + " (0)");
        headerLabel.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-font-size: 13px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: " + UiStyles.COLOR_TEXT + ";"
        );

        HBox header = new HBox(8, statusDot, headerLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 12, 0));

        emptyLabel = new Label("✨ No tasks yet");
        emptyLabel.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-font-size: 12px;"
                        + "-fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";"
                        + "-fx-padding: 24 8;"
                        + "-fx-border-color: " + UiStyles.COLOR_BORDER + ";"
                        + "-fx-border-style: dashed;"
                        + "-fx-border-width: 1.5;"
                        + "-fx-border-radius: 8;"
                        + "-fx-alignment: center;"
        );
        emptyLabel.setMaxWidth(Double.MAX_VALUE);
        emptyLabel.setAlignment(Pos.CENTER);

        tasksList = new VBox(10);
        tasksList.setFillWidth(true);
        tasksList.getChildren().add(emptyLabel);

        ScrollPane scroll = new ScrollPane(tasksList);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setPrefViewportHeight(480);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        panel = new VBox(0, header, scroll);
        panel.setMinWidth(300);
        panel.setPrefWidth(320);
        panel.setMaxWidth(340);
        panel.setPadding(new Insets(16));
        panel.setStyle(
                "-fx-background-color: rgba(244, 245, 247, 0.95);"
                        + "-fx-background-radius: 12;"
                        + "-fx-border-color: " + UiStyles.COLOR_BORDER + ";"
                        + "-fx-border-radius: 12;"
                        + "-fx-border-width: 1;"
                        + "-fx-effect: dropshadow(gaussian, rgba(9,30,66,0.06), 6, 0, 0, 2);"
        );
        HBox.setHgrow(panel, Priority.ALWAYS);
    }

    public VBox getPanel() {
        return panel;
    }

    /** Container where task card Labels are added. */
    public VBox getTasksList() {
        return tasksList;
    }

    public void refresh() {
        long taskCount = tasksList.getChildren().stream()
                .filter(n -> n instanceof Label && n != emptyLabel)
                .count();

        headerLabel.setText(title + " (" + taskCount + ")");

        boolean empty = taskCount == 0;
        emptyLabel.setVisible(empty);
        emptyLabel.setManaged(empty);
        if (empty && !tasksList.getChildren().contains(emptyLabel)) {
            tasksList.getChildren().add(0, emptyLabel);
        }
    }

    public void addTaskCard(Label card) {
        tasksList.getChildren().remove(emptyLabel);
        tasksList.getChildren().add(card);
        refresh();
    }

    public void removeTaskCard(Label card) {
        tasksList.getChildren().remove(card);
        if (tasksList.getChildren().stream().noneMatch(n -> n instanceof Label && n != emptyLabel)) {
            if (!tasksList.getChildren().contains(emptyLabel)) {
                tasksList.getChildren().add(emptyLabel);
            }
        }
        refresh();
    }
}
