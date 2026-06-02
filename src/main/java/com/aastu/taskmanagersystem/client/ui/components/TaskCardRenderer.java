package com.aastu.taskmanagersystem.client.ui.components;

import com.aastu.taskmanagersystem.client.model.Task;
import com.aastu.taskmanagersystem.client.model.TaskCardMeta;
import com.aastu.taskmanagersystem.client.model.TaskItem;
import com.aastu.taskmanagersystem.client.ui.styles.UiStyles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;

public final class TaskCardRenderer {

    private TaskCardRenderer() {}

    private static String priorityColor(String priority) {
        if ("HIGH".equals(priority)) return UiStyles.PRIORITY_HIGH;
        if ("LOW".equals(priority)) return UiStyles.PRIORITY_LOW;
        return UiStyles.PRIORITY_MEDIUM;
    }

    private static String priorityLabel(String priority) {
        if ("HIGH".equals(priority)) return "High";
        if ("LOW".equals(priority)) return "Low";
        return "Medium";
    }

    public static Label createTaskCard(Task task, Consumer<Label> onEdit, Consumer<Label> onDelete) {
        String priority = (task.getPriority() == null) ? "MEDIUM" : task.getPriority();
        String accent = priorityColor(priority);
        boolean overdue = false;

        try {
            if (!"DONE".equalsIgnoreCase(task.getStatus()) && task.getDueDate() != null && !task.getDueDate().isEmpty()) {
                java.time.LocalDate due = java.time.LocalDate.parse(task.getDueDate());
                overdue = due.isBefore(java.time.LocalDate.now());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        final boolean isOverdue = overdue;

        // ── TOP ROW ──
        Label priorityBadge = new Label(priorityLabel(priority));
        priorityBadge.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-background-color: " + accent + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 3 8;"
                        + "-fx-background-radius: 4;"
        );

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox topRow = new HBox(6, priorityBadge, topSpacer);
        topRow.setAlignment(Pos.CENTER_LEFT);

        if (task.getDueDate() != null && !task.getDueDate().isEmpty()) {
            Label dueLabel = new Label();
            if (isOverdue) {
                dueLabel.setText("⚠️ " + task.getDueDate());
                dueLabel.setStyle(UiStyles.BADGE_OVERDUE);
            } else {
                dueLabel.setText("📅 " + task.getDueDate());
                dueLabel.setStyle(
                        UiStyles.FONT_FAMILY
                                + "-fx-font-size: 10px;"
                                + "-fx-font-weight: bold;"
                                + "-fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";"
                                + "-fx-background-color: #EBECF0;"
                                + "-fx-padding: 3 8;"
                                + "-fx-background-radius: 4;"
                );
            }
            topRow.getChildren().add(dueLabel);
        }

        // ── TITLE ROW ──
        Label titleLabel = new Label(task.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-font-size: 14px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: " + UiStyles.COLOR_TEXT + ";"
        );
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // ── BOTTOM ROW ──
        String creator = task.getCreatedBy() == null || task.getCreatedBy().isBlank()
                ? "System" : task.getCreatedBy();
        Label creatorAvatar = UiStyles.createUserAvatar(creator, "#00875A");
        Label creatorName = new Label(creator);
        creatorName.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-font-size: 11px;"
                        + "-fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";"
        );
        HBox creatorBox = new HBox(4, creatorAvatar, creatorName);
        creatorBox.setAlignment(Pos.CENTER_LEFT);
        Tooltip.install(creatorBox, new Tooltip("Created by " + creator));

        String assignee = task.getAssignedTo() == null || task.getAssignedTo().isBlank()
                ? "Unassigned" : task.getAssignedTo();
        Label assigneeAvatar = UiStyles.createUserAvatar(assignee, "#0052CC");
        Label assigneeName = new Label(assignee);
        assigneeName.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-font-size: 11px;"
                        + "-fx-text-fill: " + UiStyles.COLOR_TEXT + ";"
                        + "-fx-font-weight: bold;"
        );
        HBox assigneeBox2 = new HBox(4, assigneeAvatar, assigneeName);
        assigneeBox2.setAlignment(Pos.CENTER_LEFT);
        Tooltip.install(assigneeBox2, new Tooltip("Assigned to " + assignee));

        HBox bottomRow = new HBox(12, creatorBox, assigneeBox2);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        VBox cardContent = new VBox(10);
        cardContent.getChildren().addAll(topRow, titleLabel, bottomRow);

        Label label = new Label();
        label.setGraphic(cardContent);
        label.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        label.setMinWidth(260);
        label.setPrefWidth(260);
        label.setMaxWidth(260);
        label.setMinHeight(88);

        // ── EDIT / DELETE BUTTONS ──
        Button editBtn = new Button("✏️");
        Button deleteBtn = new Button("🗑️");

        editBtn.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-background-color: transparent;"
                        + "-fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";"
                        + "-fx-padding: 2 5 2 5;"
                        + "-fx-font-size: 11px;"
                        + "-fx-background-radius: 4;"
                        + "-fx-cursor: hand;"
        );
        deleteBtn.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-background-color: transparent;"
                        + "-fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";"
                        + "-fx-padding: 2 5 2 5;"
                        + "-fx-font-size: 11px;"
                        + "-fx-background-radius: 4;"
                        + "-fx-cursor: hand;"
        );

        editBtn.setOnMouseEntered(ev -> editBtn.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-background-color: #E2EEFF;"
                        + "-fx-text-fill: #0052CC;"
                        + "-fx-padding: 2 5 2 5;"
                        + "-fx-font-size: 11px;"
                        + "-fx-background-radius: 4;"
                        + "-fx-cursor: hand;"
        ));
        editBtn.setOnMouseExited(ev -> editBtn.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-background-color: transparent;"
                        + "-fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";"
                        + "-fx-padding: 2 5 2 5;"
                        + "-fx-font-size: 11px;"
                        + "-fx-background-radius: 4;"
                        + "-fx-cursor: hand;"
        ));
        deleteBtn.setOnMouseEntered(ev -> deleteBtn.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-background-color: #FFEBE6;"
                        + "-fx-text-fill: #DE350B;"
                        + "-fx-padding: 2 5 2 5;"
                        + "-fx-font-size: 11px;"
                        + "-fx-background-radius: 4;"
                        + "-fx-cursor: hand;"
        ));
        deleteBtn.setOnMouseExited(ev -> deleteBtn.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-background-color: transparent;"
                        + "-fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";"
                        + "-fx-padding: 2 5 2 5;"
                        + "-fx-font-size: 11px;"
                        + "-fx-background-radius: 4;"
                        + "-fx-cursor: hand;"
        ));

        editBtn.setOnAction(ev -> {
            ev.consume();
            if (onEdit != null) onEdit.accept(label);
        });

        deleteBtn.setOnAction(ev -> {
            ev.consume();
            if (onDelete != null) onDelete.accept(label);
        });

        topRow.getChildren().addAll(editBtn, deleteBtn);

        String bg = isOverdue ? "#FFF5F5" : UiStyles.COLOR_SURFACE;
        String baseStyle =
                UiStyles.FONT_FAMILY
                        + "-fx-background-color: " + bg + ";"
                        + "-fx-padding: 14;"
                        + "-fx-background-radius: 10;"
                        + "-fx-border-radius: 10;"
                        + "-fx-border-color: " + (isOverdue ? UiStyles.PRIORITY_HIGH : accent) + ";"
                        + "-fx-border-width: 0 0 0 4;"
                        + "-fx-effect: dropshadow(gaussian, rgba(9,30,66,0.08), 8, 0, 0, 2);"
                        + "-fx-cursor: hand;";

        label.getProperties().put("cardBaseStyle", baseStyle);
        label.setStyle(baseStyle);

        label.setOnMouseEntered(e ->
                label.setStyle(baseStyle
                        + "-fx-background-color: " + (isOverdue ? "#FFF0F0" : "#F4F5F7") + ";"
                        + "-fx-effect: dropshadow(gaussian, rgba(9,30,66,0.15), 12, 0, 0, 4);"
                        + "-fx-translate-y: -1;")
        );
        label.setOnMouseExited(e -> label.setStyle(baseStyle));

        return label;
    }

    public static Label createTaskCard(TaskItem item, Consumer<Label> onEdit, Consumer<Label> onDelete) {
        Label label = createTaskCard(item.toTask(), onEdit, onDelete);
        label.setUserData(TaskCardMeta.fromTaskItem(item));
        label.getProperties().put("taskTitle", item.getTitle());
        label.getProperties().put("taskItem", item);
        return label;
    }
}
