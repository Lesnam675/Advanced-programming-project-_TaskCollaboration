package com.aastu.taskmanagersystem.client.ui.permissions;
import com.aastu.taskmanagersystem.client.model.Task;


import com.aastu.taskmanagersystem.client.model.TaskCardMeta;
import com.aastu.taskmanagersystem.client.ui.styles.UiStyles;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import java.util.Map;

public final class TaskPermissionManager {

    private TaskPermissionManager() {}

    private static String extractTitle(Label task) {
        Object stored = task.getProperties().get("taskTitle");
        if (stored != null) return stored.toString();
        String text = task.getText();
        if (text != null && text.contains("\n")) return text.split("\n")[0];
        return text == null ? "" : text;
    }

    public static void showPermissionDeniedAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText(title);
        Label contentLabel = new Label(message);
        contentLabel.setWrapText(true);
        contentLabel.setStyle(
                UiStyles.FONT_FAMILY
                + "-fx-font-size: 13px;"
                + "-fx-text-fill: " + UiStyles.COLOR_TEXT + ";"
        );
        alert.getDialogPane().setContent(contentLabel);
        alert.getDialogPane().setStyle(
                UiStyles.FONT_FAMILY
                + "-fx-background-color: " + UiStyles.COLOR_SURFACE + ";"
                + "-fx-border-color: " + UiStyles.COLOR_BORDER + ";"
                + "-fx-border-radius: 10;"
                + "-fx-background-radius: 10;"
        );
        alert.showAndWait();
    }

    public static boolean canModifyTask(Label task, String action, String currentUser, Map<Integer, String> editingTasks) {
        TaskCardMeta meta = TaskCardMeta.fromLabel(task);
        String creator  = meta.getCreatedBy()  == null ? "" : meta.getCreatedBy().trim();
        String assignee = meta.getAssignedTo() == null ? "" : meta.getAssignedTo().trim();
        String user = currentUser != null ? currentUser.trim() : "";

        // ── Collision / Lock check ──
        if (editingTasks.containsKey(meta.getId())) {
            String editingUser = editingTasks.get(meta.getId());
            if (!editingUser.equalsIgnoreCase(user)) {
                showPermissionDeniedAlert("Task Locked", editingUser + " is currently editing/viewing this task.");
                return false;
            }
        }

        if ("MOVE".equals(action)) {
            boolean allowed = !user.isEmpty() && !assignee.isEmpty() && assignee.equalsIgnoreCase(user);
            if (allowed) {
                System.out.println("[PERM] " + action + " ALLOWED | currentUser='" + user
                        + "' | taskAssignee='" + assignee + "' | task='" + extractTitle(task) + "'");
                return true;
            } else {
                System.out.println("[PERM] " + action + " DENIED | currentUser='" + user
                        + "' | taskAssignee='" + assignee + "' | task='" + extractTitle(task) + "' | reason="
                        + (user.isEmpty() ? "not logged in" : "user is not the assignee"));
                showPermissionDeniedAlert("Permission Denied", "Only the assignee can move this task.");
                return false;
            }
        } else {
            boolean allowed = !user.isEmpty() && !creator.isEmpty() && creator.equalsIgnoreCase(user);
            if (allowed) {
                System.out.println("[PERM] " + action + " ALLOWED | currentUser='" + user
                        + "' | taskCreator='" + creator + "' | task='" + extractTitle(task) + "'");
                return true;
            } else {
                System.out.println("[PERM] " + action + " DENIED | currentUser='" + user
                        + "' | taskCreator='" + creator + "' | task='" + extractTitle(task) + "' | reason="
                        + (user.isEmpty() ? "not logged in" : creator.isEmpty() ? "creator missing on card" : "user is not the creator"));
                if ("DELETE".equals(action)) {
                    showPermissionDeniedAlert("Permission Denied", "Only the creator can delete this task.");
                } else {
                    showPermissionDeniedAlert("Permission Denied", "Only the creator can edit this task.");
                }
                return false;
            }
        }
    }
}
