package com.aastu.taskmanagersystem.client.ui;
import com.aastu.taskmanagersystem.client.model.Task;
import com.aastu.taskmanagersystem.client.model.TaskData;
import com.aastu.taskmanagersystem.client.model.ActivityLog;
import com.aastu.taskmanagersystem.client.model.TaskCardMeta;
import com.aastu.taskmanagersystem.client.model.NotificationStore;
import com.aastu.taskmanagersystem.backend.model.UserEntity;
import com.aastu.taskmanagersystem.client.model.TaskItem;


import com.aastu.taskmanagersystem.client.api.TaskApi;
import com.aastu.taskmanagersystem.client.api.UserApi;
import com.aastu.taskmanagersystem.client.model.*;
import com.aastu.taskmanagersystem.client.socket.ClientSocketManager;
import com.aastu.taskmanagersystem.client.ui.components.KanbanColumn;
import com.aastu.taskmanagersystem.client.ui.components.TaskCardRenderer;
import com.aastu.taskmanagersystem.client.ui.dialogs.TaskDialogs;
import com.aastu.taskmanagersystem.client.ui.filter.TaskFilterManager;
import com.aastu.taskmanagersystem.client.ui.permissions.TaskPermissionManager;
import com.aastu.taskmanagersystem.client.ui.styles.UiStyles;
import com.aastu.taskmanagersystem.client.ui.styles.UiWindowHelper;
import com.aastu.taskmanagersystem.backend.database.DatabaseConnection;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.input.MouseButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main extends Application implements ClientSocketManager.SocketCallback {
    public static Main instance;
    public static final List<String> onlineUsers = new java.util.ArrayList<>();
    public static final java.util.Map<String, String> lastSeenMap = new java.util.HashMap<>();
    public static final java.util.Map<Integer, String> editingTasks = new java.util.HashMap<>();
    public static HBox activeUsersBox = new HBox(6);

    VBox todoColumn;
    VBox doingColumn;
    VBox doneColumn;

    KanbanColumn kanbanTodo;
    KanbanColumn kanbanDoing;
    KanbanColumn kanbanDone;

    ClientSocketManager socketManager;

    ComboBox<String> priorityBox;
    ComboBox<String> assigneeBox;
    DatePicker dueDatePicker;

    TextField searchField;
    ComboBox<String> priorityFilter;
    ComboBox<String> assigneeFilter;
    ComboBox<String> creatorFilter;
    ComboBox<String> statusFilter;
    ComboBox<String> sortCombo;
    ToggleButton overdueToggle;

    private final List<TaskItem> taskCache = new ArrayList<>();
    private List<String> assigneeNames = new ArrayList<>();

    private Label notifBadge;
    private VBox activityFeedBox;
    private boolean activityVisible = false;
    private VBox dashTotal, dashTodo, dashDoing, dashDone, dashOverdue, dashMine, dashAssigned;

    private static UserEntity currentUser;
    public static void setCurrentUser(UserEntity user) {
        if (user != null && user.getUsername() != null) {
            user.setUsername(user.getUsername().trim());
        }
        currentUser = user;
    }

    public static StackPane mainScreenStack;

    public static void showNotification(String text) {
        if (mainScreenStack == null) return;
        Platform.runLater(() -> {
            Label notif = new Label(text);
            notif.setWrapText(true);
            notif.setMaxWidth(280);
            notif.setStyle(
                    UiStyles.FONT_FAMILY
                            + "-fx-background-color: #0052CC;"
                            + "-fx-text-fill: white;"
                            + "-fx-font-weight: bold;"
                            + "-fx-padding: 10 16;"
                            + "-fx-background-radius: 8;"
                            + "-fx-effect: dropshadow(gaussian, rgba(9,30,66,0.15), 10, 0, 0, 4);"
            );

            mainScreenStack.getChildren().add(notif);
            StackPane.setAlignment(notif, Pos.TOP_RIGHT);
            StackPane.setMargin(notif, new Insets(80, 20, 0, 0));

            TranslateTransition tt = new TranslateTransition(Duration.millis(300), notif);
            tt.setFromX(300);
            tt.setToX(0);

            FadeTransition ft = new FadeTransition(Duration.millis(300), notif);
            ft.setFromValue(0);
            ft.setToValue(1);

            ParallelTransition show = new ParallelTransition(tt, ft);
            show.play();

            PauseTransition delay = new PauseTransition(Duration.seconds(3.5));
            delay.setOnFinished(e -> {
                TranslateTransition outTt = new TranslateTransition(Duration.millis(300), notif);
                outTt.setToX(300);
                FadeTransition outFt = new FadeTransition(Duration.millis(300), notif);
                outFt.setToValue(0);
                ParallelTransition hide = new ParallelTransition(outTt, outFt);
                hide.setOnFinished(ev -> mainScreenStack.getChildren().remove(notif));
                hide.play();
            });
            delay.play();
        });
    }

    public static void showSlideInNotification(String title, String message) {
        showNotification(title + " — " + message);
    }

    public void updateActiveUsersUi() {
        Platform.runLater(() -> {
            activeUsersBox.getChildren().clear();
            for (String username : onlineUsers) {
                if (username.equalsIgnoreCase(currentUsername())) continue;

                Label avatar = UiStyles.createUserAvatar(username, "#36B37E");

                Region greenDot = new Region();
                greenDot.setStyle(UiStyles.DOT_ONLINE);

                StackPane stack = new StackPane(avatar, greenDot);
                StackPane.setAlignment(greenDot, Pos.BOTTOM_RIGHT);

                Tooltip.install(stack, new Tooltip(username + " (Online)"));

                activeUsersBox.getChildren().add(stack);
            }
        });
    }

    private void refreshAllColumns() {
        kanbanTodo.refresh();
        kanbanDoing.refresh();
        kanbanDone.refresh();
    }

    private KanbanColumn kanbanForList(VBox tasksList) {
        if (tasksList == todoColumn) return kanbanTodo;
        if (tasksList == doingColumn) return kanbanDoing;
        return kanbanDone;
    }

    private Label renderTaskCard(TaskItem item) {
        Label card = TaskCardRenderer.createTaskCard(item, this::openEditDialog, this::deleteTask);
        enableTaskMovement(card, item.getStatus().equals("TODO") ? todoColumn : item.getStatus().equals("DOING") ? doingColumn : doneColumn);
        return card;
    }

    private TaskItem itemFromCard(Label card) {
        Object stored = card.getProperties().get("taskItem");
        if (stored instanceof TaskItem item) return item;
        TaskCardMeta meta = TaskCardMeta.fromLabel(card);
        TaskItem item = new TaskItem();
        item.setId(meta.getId());
        item.setTitle(extractTitle(card));
        item.setStatus(meta.getStatus());
        item.setPriority(meta.getPriority());
        item.setDueDate(meta.getDueDate());
        item.setAssignedTo(meta.getAssignedTo());
        item.setCreatedBy(meta.getCreatedBy());
        item.setCreatedAt(meta.getCreatedAt());
        return item;
    }

    private void reloadCacheFromApi() {
        taskCache.clear();
        List<TaskData> tasks = TaskApi.getAllTasks();
        if (tasks != null) {
            for (TaskData data : tasks) {
                taskCache.add(TaskItem.fromData(data));
            }
        }
    }

    private void applyFiltersAndRender() {
        todoColumn.getChildren().clear();
        doingColumn.getChildren().clear();
        doneColumn.getChildren().clear();
        refreshAllColumns();

        String search = searchField != null ? searchField.getText() : "";
        String priority = (priorityFilter != null && priorityFilter.getValue() != null) ? priorityFilter.getValue() : "All";
        String assignee = (assigneeFilter != null && assigneeFilter.getValue() != null) ? assigneeFilter.getValue() : "All";
        String creator = (creatorFilter != null && creatorFilter.getValue() != null) ? creatorFilter.getValue() : "All";
        String status = (statusFilter != null && statusFilter.getValue() != null) ? statusFilter.getValue() : "All";
        String sort = (sortCombo != null && sortCombo.getValue() != null) ? sortCombo.getValue() : "Due soon first";
        boolean overdueOnly = overdueToggle != null && overdueToggle.isSelected();

        List<TaskItem> filtered = TaskFilterManager.getFilteredTasks(
                taskCache, search, priority, assignee, creator, status, sort, overdueOnly, currentUsername()
        );

        for (TaskItem item : filtered) {
            Label card = renderTaskCard(item);
            String st = item.getStatus();
            if ("TODO".equals(st)) {
                kanbanTodo.addTaskCard(card);
            } else if ("DOING".equals(st)) {
                kanbanDoing.addTaskCard(card);
            } else {
                kanbanDone.addTaskCard(card);
            }
        }
        refreshAllColumns();
        updateDashboard();
    }

    private void upsertCacheItem(TaskItem item) {
        for (int i = 0; i < taskCache.size(); i++) {
            if (taskCache.get(i).getId() == item.getId()
                    || taskCache.get(i).getTitle().equals(item.getTitle())) {
                taskCache.set(i, item);
                return;
            }
        }
        taskCache.add(item);
    }

    private void removeFromCacheByTitle(String title) {
        taskCache.removeIf(t -> t.getTitle().equals(title));
    }

    private void broadcastEdit(TaskItem item) {
        if (socketManager != null && socketManager.getWriter() != null) {
            socketManager.getWriter().println(
                    "EDIT|"
                            + item.getId() + "|"
                            + item.getTitle() + "|"
                            + item.getStatus() + "|"
                            + item.getPriority() + "|"
                            + item.getDueDate() + "|"
                            + item.getAssignedTo() + "|"
                            + item.getCreatedBy() + "|"
                            + item.getCreatedAt() + "|"
                            + currentUsername()
            );
        }
    }

    private void openEditDialog(Label card) {
        if (!TaskPermissionManager.canModifyTask(card, "EDIT", currentUsername(), editingTasks)) return;
        TaskItem current = itemFromCard(card);

        // Conflicting edit prevention
        if (editingTasks.containsKey(current.getId())) {
            String editingUser = editingTasks.get(current.getId());
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Task Locked");
            alert.setHeaderText("Conflicting Edit Blocked");
            alert.setContentText(editingUser + " is currently editing this task. Please wait.");
            alert.showAndWait();
            return;
        }

        Optional<TaskItem> edited = TaskDialogs.showEditDialog(current, assigneeNames);
        if (edited.isEmpty()) return;
        TaskItem updated = edited.get();

        String now = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        updated.setUpdatedAt(now);

        TaskApi.updateTaskById(
                updated.getId(), updated.getTitle(), updated.getStatus(),
                updated.getPriority(), updated.getDueDate(),
                updated.getAssignedTo(), updated.getCreatedBy(),
                updated.getDescription(), updated.getChecklist(),
                updated.getProgress(), currentUsername()
        );
        upsertCacheItem(updated);
        broadcastEdit(updated);

        applyFiltersAndRender();
        updateActivityFeed();
        updateNotifBadge();
    }

    private void deleteTask(Label card) {
        if (!TaskPermissionManager.canModifyTask(card, "DELETE", currentUsername(), editingTasks)) return;
        String title = extractTitle(card);
        String assignedTo = TaskCardMeta.fromLabel(card).getAssignedTo();

        ActivityLog.addEntry(currentUsername(), ActivityLog.Action.DELETED, title);
        if (assignedTo != null && !assignedTo.isBlank()
                && assignedTo.equalsIgnoreCase(currentUsername())) {
            NotificationStore.add("Your task \"" + title + "\" was deleted", title);
        }

        TaskApi.deleteTask(title, currentUsername());
        removeFromCacheByTitle(title);
        if (socketManager != null) {
            socketManager.sendDeleteTask(title);
        }
        applyFiltersAndRender();
        updateActivityFeed();
        updateNotifBadge();
        System.out.println("[DELETE] removed task '" + title + "'");
    }

    private String currentUsername() {
        return currentUser != null && currentUser.getUsername() != null
                ? currentUser.getUsername().trim() : "";
    }

    private void moveTaskCard(Label task, VBox fromColumn, VBox toColumn, String newStatus) {
        TaskItem item = itemFromCard(task);
        String title    = extractTitle(task);
        String priority = getPriorityFromTask(task);
        String creator  = item.getCreatedBy();

        TaskApi.updateTask(
                title,
                newStatus,
                priority,
                item.getDueDate(),
                item.getAssignedTo(),
                creator,
                item.getDescription() == null ? "" : item.getDescription(),
                item.getChecklist() == null ? "" : item.getChecklist(),
                item.getProgress(),
                currentUsername()
        );

        if (socketManager != null) {
            socketManager.sendMoveTask(title, newStatus, priority);
        }
        item.setStatus(newStatus);
        String now = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        item.setUpdatedAt(now);
        upsertCacheItem(item);

        kanbanForList(fromColumn).removeTaskCard(task);
        kanbanForList(toColumn).addTaskCard(task);
        enableTaskMovement(task, toColumn);
        refreshAllColumns();

        ActivityLog.addEntry(currentUsername(), ActivityLog.Action.MOVED, title + " → " + newStatus);

        String assignee = item.getAssignedTo();
        if (assignee != null && assignee.equalsIgnoreCase(currentUsername())) {
            NotificationStore.add("Your task \"" + title + "\" was moved to " + newStatus, title);
        }

        updateActivityFeed();
        updateNotifBadge();
        updateDashboard();
        System.out.println("[MOVE] '" + title + "' -> " + newStatus + " | creator='" + creator + "'");
    }

    private String getPriorityFromTask(Label task) {
        String style = task.getStyle();
        if (style.contains(UiStyles.PRIORITY_HIGH) || style.contains("#E05252")) return "HIGH";
        if (style.contains(UiStyles.PRIORITY_LOW)  || style.contains("#23B27D")) return "LOW";
        return "MEDIUM";
    }

    private void enableTaskMovement(Label task, VBox currentColumn) {
        PauseTransition clickDelay = new PauseTransition(Duration.millis(250));
        clickDelay.setOnFinished(ev -> {
            Object stored = task.getProperties().get("pendingClick");
            if (stored instanceof javafx.scene.input.MouseEvent mouseEvent) {
                handleTaskCardClick(task, currentColumn, mouseEvent);
            }
        });

        task.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
                clickDelay.stop();
                openEditDialog(task);
                return;
            }
            if (e.isControlDown() && e.getButton() == MouseButton.PRIMARY) {
                clickDelay.stop();
                TaskDialogs.showDetailsDialog(itemFromCard(task), currentUsername(), socketManager != null ? socketManager.getWriter() : null, t -> reloadCacheFromApi());
                return;
            }
            if (e.getButton() == MouseButton.SECONDARY) {
                clickDelay.stop();
                handleTaskCardClick(task, currentColumn, e);
                return;
            }
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1) {
                task.getProperties().put("pendingClick", e);
                clickDelay.playFromStart();
            }
        });
    }

    private void handleTaskCardClick(Label task, VBox currentColumn, javafx.scene.input.MouseEvent e) {
        if (e.getButton() == MouseButton.SECONDARY) {
            deleteTask(task);
            return;
        }

        if (e.getButton() != MouseButton.PRIMARY) return;
        if (!TaskPermissionManager.canModifyTask(task, "MOVE", currentUsername(), editingTasks)) return;

        if (e.isShiftDown()) {
            if (currentColumn == doingColumn) moveTaskCard(task, doingColumn, todoColumn, "TODO");
            else if (currentColumn == doneColumn) moveTaskCard(task, doneColumn, doingColumn, "DOING");
        } else {
            if (currentColumn == todoColumn) moveTaskCard(task, todoColumn, doingColumn, "DOING");
            else if (currentColumn == doingColumn) moveTaskCard(task, doingColumn, doneColumn, "DONE");
        }
    }

    private void loadTasks() {
        reloadCacheFromApi();
        applyFiltersAndRender();
    }

    // ── SOCKET CALLBACKS ───────────────────────────────────
    @Override
    public void onNewTask(String taskText, String priority, String dueDate, String assignedTo, String createdBy) {
        boolean exists = taskCache.stream().anyMatch(t -> t.getTitle().equals(taskText));
        if (exists) return;
        TaskItem item = new TaskItem();
        item.setTitle(taskText);
        item.setStatus("TODO");
        item.setPriority(priority);
        item.setDueDate(dueDate == null ? "" : dueDate);
        item.setAssignedTo(assignedTo == null ? "" : assignedTo);
        item.setCreatedBy(createdBy == null ? "" : createdBy);
        item.setCreatedAt(java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        upsertCacheItem(item);
        reloadCacheFromApi();

        if (assignedTo != null && assignedTo.equalsIgnoreCase(currentUsername()) && !createdBy.equalsIgnoreCase(currentUsername())) {
            NotificationStore.add("Task \"" + taskText + "\" was assigned to you", taskText);
            showSlideInNotification("Task Assigned", "Task \"" + taskText + "\" was assigned to you");
        }

        applyFiltersAndRender();
        updateActivityFeed();
        updateNotifBadge();
    }

    @Override
    public void onMoveTask(String title, String status, String priority) {
        taskCache.stream()
                .filter(t -> t.getTitle().equals(title))
                .findFirst()
                .ifPresent(t -> {
                    String assignedTo = t.getAssignedTo();
                    t.setStatus(status);
                    t.setPriority(priority);
                    t.setUpdatedAt(java.time.LocalDateTime.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    upsertCacheItem(t);

                    if (assignedTo != null && assignedTo.equalsIgnoreCase(currentUsername())) {
                        NotificationStore.add("Your task \"" + title + "\" was moved to " + status, title);
                        showSlideInNotification("Task Moved", "Your task \"" + title + "\" was moved to " + status);
                    }
                });
        applyFiltersAndRender();
        updateActivityFeed();
        updateNotifBadge();
    }

    @Override
    public void onEditTask(int taskId, String newTitle, String newStatus, String newPriority, String newDueDate, String newAssignee, String newCreator, String newCreatedAt, String actor) {
        taskCache.stream()
                .filter(t -> t.getId() == taskId)
                .findFirst()
                .ifPresent(old -> {
                    if (old.getAssignedTo() != null
                            && !old.getAssignedTo().equalsIgnoreCase(newAssignee)
                            && newAssignee.equalsIgnoreCase(currentUsername())
                            && !actor.equalsIgnoreCase(currentUsername())) {
                        NotificationStore.add("Task \"" + newTitle + "\" was assigned to you", newTitle);
                        showSlideInNotification("Task Assigned", "Task \"" + newTitle + "\" was assigned to you");
                    } else if (old.getAssignedTo() != null
                            && old.getAssignedTo().equalsIgnoreCase(currentUsername())
                            && !actor.equalsIgnoreCase(currentUsername())) {
                        NotificationStore.add("Your task \"" + newTitle + "\" was edited", newTitle);
                        showSlideInNotification("Task Edited", "Your task \"" + newTitle + "\" was edited");
                    }

                    old.setTitle(newTitle);
                    old.setStatus(newStatus);
                    old.setPriority(newPriority);
                    old.setDueDate(newDueDate);
                    old.setAssignedTo(newAssignee);
                    old.setCreatedBy(newCreator);
                    old.setCreatedAt(newCreatedAt);
                    old.setUpdatedAt(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    upsertCacheItem(old);
                });

        reloadCacheFromApi();
        applyFiltersAndRender();
        updateActivityFeed();
        updateNotifBadge();

        if (TaskDialogs.activeDetailsTaskId != null && TaskDialogs.activeDetailsTaskId == taskId) {
            if (TaskDialogs.detailsRefresher != null) {
                TaskDialogs.detailsRefresher.run();
            }
        }
    }

    @Override
    public void onDeleteTask(String title) {
        taskCache.stream()
                .filter(t -> t.getTitle().equals(title))
                .findFirst()
                .ifPresent(t -> {
                    if (t.getAssignedTo() != null
                            && t.getAssignedTo().equalsIgnoreCase(currentUsername())) {
                        NotificationStore.add("Your task \"" + title + "\" was deleted", title);
                        showSlideInNotification("Task Deleted", "Your task \"" + title + "\" was deleted");
                    }
                });
        removeFromCacheByTitle(title);
        applyFiltersAndRender();
        updateActivityFeed();
        updateNotifBadge();
    }

    @Override
    public void onPresenceOnline(String user) {
        if (!onlineUsers.contains(user)) {
            onlineUsers.add(user);
            updateActiveUsersUi();
        }
    }

    @Override
    public void onPresenceOffline(String user) {
        onlineUsers.remove(user);
        lastSeenMap.put(user.toLowerCase(), java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        updateActiveUsersUi();
    }

    @Override
    public void onPresenceList(String usersList) {
        if (!usersList.isEmpty()) {
            String[] users = usersList.split(",");
            for (String u : users) {
                String cleanUser = u.trim();
                if (!cleanUser.isEmpty() && !onlineUsers.contains(cleanUser) && !cleanUser.equalsIgnoreCase(currentUsername())) {
                    onlineUsers.add(cleanUser);
                }
            }
            updateActiveUsersUi();
        }
    }

    @Override
    public void onTyping(String action, String user, int taskId) {
        if ("EDIT_START".equals(action)) {
            editingTasks.put(taskId, user);
        } else if ("EDIT_END".equals(action)) {
            editingTasks.remove(taskId);
        } else if ("COMMENT_START".equals(action)) {
            if (TaskDialogs.activeDetailsTaskId != null && TaskDialogs.activeDetailsTaskId == taskId) {
                if (TaskDialogs.activeTypingLabel != null) {
                    TaskDialogs.activeTypingLabel.setText(user + " is typing...");
                }
            }
        } else if ("COMMENT_END".equals(action)) {
            if (TaskDialogs.activeDetailsTaskId != null && TaskDialogs.activeDetailsTaskId == taskId) {
                if (TaskDialogs.activeTypingLabel != null) {
                    TaskDialogs.activeTypingLabel.setText("");
                }
            }
        }
    }

    @Override
    public void onCommentSync(int taskId) {
        if (TaskDialogs.activeDetailsTaskId != null && TaskDialogs.activeDetailsTaskId == taskId) {
            if (TaskDialogs.commentsRefresher != null) {
                TaskDialogs.commentsRefresher.run();
            }
            if (TaskDialogs.historyRefresher != null) {
                TaskDialogs.historyRefresher.run();
            }
        }
    }

    private String extractTitle(Label task) {
        Object stored = task.getProperties().get("taskTitle");
        if (stored != null) return stored.toString();
        String text = task.getText();
        if (text != null && text.contains("\n")) return text.split("\n")[0];
        return text == null ? "" : text;
    }

    // =========================================================
    // PHASE 4 — DASHBOARD PANEL
    // =========================================================
    private HBox buildDashboardPanel() {
        dashTotal    = makeDashStat("Total Tasks",    "0", "#0052CC");
        dashTodo     = makeDashStat("TODO",           "0", UiStyles.ACCENT_TODO);
        dashDoing    = makeDashStat("DOING",          "0", UiStyles.ACCENT_DOING);
        dashDone     = makeDashStat("DONE",           "0", UiStyles.ACCENT_DONE);
        dashOverdue  = makeDashStat("Overdue",        "0", UiStyles.PRIORITY_HIGH);
        dashMine     = makeDashStat("Created by Me",  "0", "#5243AA");
        dashAssigned = makeDashStat("Assigned to Me", "0", "#00B8D9");

        HBox dash = new HBox(10,
                wrapStatCard(dashTotal,    "#0052CC"),
                wrapStatCard(dashTodo,     UiStyles.ACCENT_TODO),
                wrapStatCard(dashDoing,    UiStyles.ACCENT_DOING),
                wrapStatCard(dashDone,     UiStyles.ACCENT_DONE),
                wrapStatCard(dashOverdue,  UiStyles.PRIORITY_HIGH),
                wrapStatCard(dashMine,     "#5243AA"),
                wrapStatCard(dashAssigned, "#00B8D9")
        );
        dash.setAlignment(Pos.CENTER_LEFT);
        dash.setPadding(new Insets(10, 4, 4, 4));
        return dash;
    }

    private VBox makeDashStat(String labelText, String count, String accent) {
        Label countLbl = new Label(count);
        countLbl.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-font-size: 22px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: " + accent + ";"
        );
        Label nameLbl = new Label(labelText);
        nameLbl.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-font-size: 11px;"
                        + "-fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";"
        );
        VBox box = new VBox(2, countLbl, nameLbl);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setUserData(countLbl);
        return box;
    }

    private HBox wrapStatCard(VBox inner, String accent) {
        HBox card = new HBox(inner);
        card.setStyle(UiStyles.statCard(accent));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMinWidth(110);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private void updateDashboard() {
        if (dashTotal == null) return;
        String me = currentUsername();
        long total    = taskCache.size();
        long todo     = taskCache.stream().filter(t -> "TODO".equalsIgnoreCase(t.getStatus())).count();
        long doing    = taskCache.stream().filter(t -> "DOING".equalsIgnoreCase(t.getStatus())).count();
        long done     = taskCache.stream().filter(t -> "DONE".equalsIgnoreCase(t.getStatus())).count();
        long overdue  = taskCache.stream().filter(TaskFilterManager::isOverdue).count();
        long mine     = taskCache.stream().filter(t -> t.getCreatedBy() != null && t.getCreatedBy().equalsIgnoreCase(me)).count();
        long assigned = taskCache.stream().filter(t -> t.getAssignedTo() != null && t.getAssignedTo().equalsIgnoreCase(me)).count();

        setStatCount(dashTotal,    total);
        setStatCount(dashTodo,     todo);
        setStatCount(dashDoing,    doing);
        setStatCount(dashDone,     done);
        setStatCount(dashOverdue,  overdue);
        setStatCount(dashMine,     mine);
        setStatCount(dashAssigned, assigned);
    }

    private void setStatCount(VBox stat, long value) {
        if (stat == null) return;
        Object ud = stat.getUserData();
        if (ud instanceof Label lbl) {
            lbl.setText(String.valueOf(value));
        }
    }

    private VBox buildActivityFeedPanel() {
        activityFeedBox = new VBox(6);
        activityFeedBox.setPadding(new Insets(6));
        activityFeedBox.setFillWidth(true);

        ScrollPane scroll = new ScrollPane(activityFeedBox);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setPrefHeight(180);
        scroll.setMaxHeight(180);

        Label header = new Label("📋 Recent Activity");
        header.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-font-size: 12px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";"
        );

        VBox panel = new VBox(6, header, scroll);
        panel.setPadding(new Insets(10, 14, 10, 14));
        panel.setStyle(
                "-fx-background-color: " + UiStyles.COLOR_SURFACE + ";"
                        + "-fx-border-color: " + UiStyles.COLOR_BORDER + ";"
                        + "-fx-border-radius: 10;"
                        + "-fx-background-radius: 10;"
                        + "-fx-border-width: 1;"
                        + "-fx-effect: dropshadow(gaussian, rgba(9,30,66,0.06), 8, 0, 0, 2);"
        );
        panel.setManaged(false);
        panel.setVisible(false);
        return panel;
    }

    private void updateActivityFeed() {
        if (activityFeedBox == null) return;
        activityFeedBox.getChildren().clear();
        List<ActivityLog> entries = ActivityLog.getRecent(50);
        if (entries.isEmpty()) {
            Label empty = new Label("No activity yet.");
            empty.setStyle(UiStyles.FONT_FAMILY + "-fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + "; -fx-font-size: 12px;");
            activityFeedBox.getChildren().add(empty);
            return;
        }
        for (ActivityLog entry : entries) {
            Label userLbl = new Label(entry.getUsername());
            userLbl.setStyle(UiStyles.FONT_FAMILY + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + UiStyles.COLOR_TEXT + ";");

            Label actionLbl = new Label(entry.getAction().getLabel());
            actionLbl.setStyle(UiStyles.FONT_FAMILY + "-fx-font-size: 11px; -fx-text-fill: #0052CC; -fx-font-weight: bold;");

            Label taskLbl = new Label("\"" + entry.getTaskName() + "\"");
            taskLbl.setStyle(UiStyles.FONT_FAMILY + "-fx-font-size: 11px; -fx-text-fill: " + UiStyles.COLOR_TEXT + ";");
            taskLbl.setWrapText(false);

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Label timeLbl = new Label(entry.getFormattedDate());
            timeLbl.setStyle(UiStyles.FONT_FAMILY + "-fx-font-size: 10px; -fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";");

            HBox row = new HBox(6, userLbl, actionLbl, taskLbl, sp, timeLbl);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(UiStyles.ACTIVITY_ENTRY);

            activityFeedBox.getChildren().add(row);
        }
    }

    private StackPane buildNotifBell(Stage ownerStage) {
        Button bellBtn = new Button("🔔");
        bellBtn.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-background-color: transparent;"
                        + "-fx-font-size: 18px;"
                        + "-fx-cursor: hand;"
                        + "-fx-padding: 4 8;"
                        + "-fx-background-radius: 8;"
        );
        bellBtn.setOnMouseEntered(e -> bellBtn.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-background-color: #F4F5F7;"
                        + "-fx-font-size: 18px;"
                        + "-fx-cursor: hand;"
                        + "-fx-padding: 4 8;"
                        + "-fx-background-radius: 8;"
        ));
        bellBtn.setOnMouseExited(e -> bellBtn.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-background-color: transparent;"
                        + "-fx-font-size: 18px;"
                        + "-fx-cursor: hand;"
                        + "-fx-padding: 4 8;"
                        + "-fx-background-radius: 8;"
        ));

        notifBadge = new Label("0");
        notifBadge.setStyle(UiStyles.BADGE_PILL);
        notifBadge.setVisible(false);
        notifBadge.setManaged(false);
        StackPane.setAlignment(notifBadge, Pos.TOP_RIGHT);

        StackPane bell = new StackPane(bellBtn, notifBadge);
        bell.setAlignment(Pos.CENTER);

        bellBtn.setOnAction(e -> showNotifPopup(ownerStage, bell));
        return bell;
    }

    private void showNotifPopup(Stage owner, StackPane anchor) {
        List<NotificationStore> all = NotificationStore.getAll();

        VBox list = new VBox(6);
        list.setPadding(new Insets(10));
        list.setMinWidth(320);
        list.setPrefWidth(360);

        if (all.isEmpty()) {
            Label empty = new Label("No notifications yet.");
            empty.setStyle(UiStyles.FONT_FAMILY + "-fx-font-size: 12px; -fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";");
            list.getChildren().add(empty);
        } else {
            for (NotificationStore n : all) {
                Label msg = new Label((n.isRead() ? "" : "🔵 ") + n.getMessage());
                msg.setWrapText(true);
                msg.setMaxWidth(300);
                msg.setStyle(UiStyles.FONT_FAMILY
                        + "-fx-font-size: 12px;"
                        + "-fx-text-fill: " + (n.isRead() ? UiStyles.COLOR_TEXT_MUTED : UiStyles.COLOR_TEXT) + ";");

                Label time = new Label(n.getFormattedTime());
                time.setStyle(UiStyles.FONT_FAMILY + "-fx-font-size: 10px; -fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";");

                VBox entry = new VBox(2, msg, time);
                entry.setStyle(n.isRead() ? UiStyles.ACTIVITY_ENTRY : UiStyles.NOTIF_ENTRY);
                list.getChildren().add(entry);
            }
        }

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPrefHeight(Math.min(all.size() * 64 + 20, 320));
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        Label header = new Label("🔔 Notifications");
        header.setStyle(UiStyles.FONT_FAMILY
                + "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + UiStyles.COLOR_TEXT + ";");

        VBox popup = new VBox(8, header, scroll);
        popup.setPadding(new Insets(12));
        popup.setStyle(
                "-fx-background-color: " + UiStyles.COLOR_SURFACE + ";"
                        + "-fx-border-color: " + UiStyles.COLOR_BORDER + ";"
                        + "-fx-border-radius: 10;"
                        + "-fx-background-radius: 10;"
                        + "-fx-border-width: 1;"
                        + "-fx-effect: dropshadow(gaussian, rgba(9,30,66,0.14), 16, 0, 0, 4);"
        );

        javafx.stage.Popup popupWindow = new javafx.stage.Popup();
        popupWindow.setAutoHide(true);
        popupWindow.getContent().add(popup);

        javafx.geometry.Bounds bounds = anchor.localToScreen(anchor.getBoundsInLocal());
        popupWindow.show(owner, bounds.getMinX() - 200, bounds.getMaxY() + 4);

        NotificationStore.markAllRead();
        updateNotifBadge();
    }

    private void updateNotifBadge() {
        if (notifBadge == null) return;
        int count = NotificationStore.unreadCount();
        if (count == 0) {
            notifBadge.setVisible(false);
            notifBadge.setManaged(false);
        } else {
            notifBadge.setText(count > 99 ? "99+" : String.valueOf(count));
            notifBadge.setVisible(true);
            notifBadge.setManaged(true);
        }
    }

    @Override
    public void start(Stage stage) {
        instance = this;

        // ── SOCKET CONNECTION ──────────────────────────────────
        socketManager = new ClientSocketManager("localhost", 5000, this);
        if (currentUser != null && currentUser.getUsername() != null) {
            socketManager.sendPresence(currentUser.getUsername());
        }
        socketManager.startListener();

        // ── KANBAN COLUMNS ─────────────────────────────────────
        kanbanTodo  = new KanbanColumn("TO DO",  UiStyles.ACCENT_TODO);
        kanbanDoing = new KanbanColumn("DOING",  UiStyles.ACCENT_DOING);
        kanbanDone  = new KanbanColumn("DONE",   UiStyles.ACCENT_DONE);

        todoColumn  = kanbanTodo.getTasksList();
        doingColumn = kanbanDoing.getTasksList();
        doneColumn  = kanbanDone.getTasksList();

        // ── FILTERS & SEARCH ───────────────────────────────────
        searchField = new TextField();
        searchField.setPromptText("🔍 Search tasks...");
        UiStyles.applyFieldEffects(searchField);
        searchField.textProperty().addListener((obs, old, val) -> applyFiltersAndRender());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        priorityFilter = new ComboBox<>();
        priorityFilter.getItems().addAll("All", "HIGH", "MEDIUM", "LOW");
        priorityFilter.setValue("All");
        priorityFilter.setPromptText("Priority");
        UiStyles.applyFieldEffects(priorityFilter);
        priorityFilter.setOnAction(e -> applyFiltersAndRender());

        assigneeFilter = new ComboBox<>();
        assigneeFilter.getItems().add("All");
        assigneeFilter.setValue("All");
        assigneeFilter.setPromptText("Assignee");
        UiStyles.applyFieldEffects(assigneeFilter);
        assigneeFilter.setOnAction(e -> applyFiltersAndRender());

        creatorFilter = new ComboBox<>();
        creatorFilter.getItems().add("All");
        creatorFilter.setValue("All");
        creatorFilter.setPromptText("Creator");
        UiStyles.applyFieldEffects(creatorFilter);
        creatorFilter.setOnAction(e -> applyFiltersAndRender());

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "TODO", "DOING", "DONE");
        statusFilter.setValue("All");
        statusFilter.setPromptText("Status");
        UiStyles.applyFieldEffects(statusFilter);
        statusFilter.setOnAction(e -> applyFiltersAndRender());

        sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Due soon first", "Oldest first", "Newest first");
        sortCombo.setValue("Due soon first");
        UiStyles.applyFieldEffects(sortCombo);
        sortCombo.setOnAction(e -> applyFiltersAndRender());

        overdueToggle = new ToggleButton("⚠️ Overdue");
        overdueToggle.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-background-color: transparent;"
                        + "-fx-text-fill: " + UiStyles.PRIORITY_HIGH + ";"
                        + "-fx-border-color: " + UiStyles.PRIORITY_HIGH + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 6 12;"
                        + "-fx-font-size: 12px;"
                        + "-fx-cursor: hand;"
        );
        overdueToggle.selectedProperty().addListener((obs, old, val) -> {
            if (val) {
                overdueToggle.setStyle(
                        UiStyles.FONT_FAMILY
                                + "-fx-background-color: " + UiStyles.PRIORITY_HIGH + ";"
                                + "-fx-text-fill: white;"
                                + "-fx-border-color: " + UiStyles.PRIORITY_HIGH + ";"
                                + "-fx-border-radius: 8;"
                                + "-fx-background-radius: 8;"
                                + "-fx-padding: 6 12;"
                                + "-fx-font-size: 12px;"
                                + "-fx-cursor: hand;"
                );
            } else {
                overdueToggle.setStyle(
                        UiStyles.FONT_FAMILY
                                + "-fx-background-color: transparent;"
                                + "-fx-text-fill: " + UiStyles.PRIORITY_HIGH + ";"
                                + "-fx-border-color: " + UiStyles.PRIORITY_HIGH + ";"
                                + "-fx-border-radius: 8;"
                                + "-fx-background-radius: 8;"
                                + "-fx-padding: 6 12;"
                                + "-fx-font-size: 12px;"
                                + "-fx-cursor: hand;"
                );
            }
            applyFiltersAndRender();
        });

        Label filterHint = new Label("Filters:");
        filterHint.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-font-size: 12px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";"
        );

        HBox filterRow = new HBox(8,
                filterHint, searchField, priorityFilter, assigneeFilter, creatorFilter,
                statusFilter, sortCombo, overdueToggle
        );
        filterRow.setAlignment(Pos.CENTER_LEFT);
        filterRow.setPadding(new Insets(6, 4, 6, 4));

        // ── INPUT UI ───────────────────────────────────────────
        TextField taskInput = new TextField();

        priorityBox = new ComboBox<>();
        assigneeBox = new ComboBox<>();

        UiStyles.applyFieldEffects(assigneeBox);
        assigneeBox.setMinWidth(110);
        assigneeBox.setPrefWidth(130);

        List<UserEntity> users = UserApi.getAllUsers();
        assigneeNames = new ArrayList<>();
        for (UserEntity user : users) {
            if (user.getUsername() != null) {
                String name = user.getUsername().trim();
                if (!name.isEmpty() && !assigneeNames.contains(name)) {
                    assigneeNames.add(name);
                }
            }
        }
        assigneeNames.sort(String.CASE_INSENSITIVE_ORDER);

        assigneeFilter.getItems().clear();
        assigneeFilter.getItems().add("All");
        creatorFilter.getItems().clear();
        creatorFilter.getItems().add("All");

        for (String name : assigneeNames) {
            assigneeBox.getItems().add(name);
            assigneeFilter.getItems().add(name);
            creatorFilter.getItems().add(name);
        }
        assigneeFilter.setValue("All");
        creatorFilter.setValue("All");
        if (!assigneeBox.getItems().isEmpty()) {
            assigneeBox.setValue(assigneeBox.getItems().get(0));
        }

        dueDatePicker = new DatePicker();
        UiStyles.applyFieldEffects(dueDatePicker);
        dueDatePicker.setMinWidth(110);
        dueDatePicker.setPrefWidth(125);

        priorityBox.getItems().addAll("HIGH", "MEDIUM", "LOW");
        priorityBox.setValue("MEDIUM");
        UiStyles.applyFieldEffects(priorityBox);
        priorityBox.setMinWidth(90);
        priorityBox.setPrefWidth(100);

        UiStyles.applyFieldEffects(taskInput);
        taskInput.setPromptText("What needs to be done?");
        taskInput.setMinWidth(150);
        taskInput.setPrefWidth(220);
        HBox.setHgrow(taskInput, Priority.ALWAYS);
        taskInput.setMaxWidth(Double.MAX_VALUE);

        Button addButton    = new Button("Add Task");
        Button logoutButton = new Button("Logout");
        UiStyles.applyButtonEffects(addButton,    true);
        UiStyles.applyButtonEffects(logoutButton, false);

        logoutButton.setOnAction(e -> {
            if (socketManager != null && currentUser != null) {
                socketManager.sendOfflinePresence(currentUser.getUsername());
            }
            currentUser = null;
            stage.close();
            try {
                new LoginScreen().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        addButton.setOnAction(e -> {
            String taskText = taskInput.getText().trim();
            if (!taskText.isEmpty() && currentUser != null) {
                String assignee = assigneeBox.getValue() != null
                        ? assigneeBox.getValue()
                        : currentUser.getUsername();

                TaskApi.addTask(
                        taskText, "TODO",
                        priorityBox.getValue(),
                        dueDatePicker.getValue() != null ? dueDatePicker.getValue().toString() : "",
                        assignee,
                        currentUser.getUsername(),
                        "", // description
                        "", // checklist
                        0   // progress
                );

                if (socketManager != null) {
                    socketManager.sendNewTask(taskText, priorityBox.getValue(),
                            (dueDatePicker.getValue() != null ? dueDatePicker.getValue().toString() : ""),
                            assignee, currentUser.getUsername());
                }

                ActivityLog.addEntry(currentUsername(), ActivityLog.Action.CREATED, taskText);

                if (assignee.equalsIgnoreCase(currentUsername())) {
                    NotificationStore.add("Task \"" + taskText + "\" was assigned to you", taskText);
                }

                taskInput.clear();
                reloadCacheFromApi();
                applyFiltersAndRender();
                updateActivityFeed();
                updateNotifBadge();
            }
        });

        // ── LOAD INITIAL DATA ───────────────────────────────────
        loadTasks();

        // ── DASHBOARD PANEL ─────────────────────────────────────
        HBox dashPanel = buildDashboardPanel();

        // ── ACTIVITY FEED PANEL ─────────────────────────────────
        VBox activityPanel = buildActivityFeedPanel();
        updateActivityFeed();

        // ── NOTIFICATION BELL ────────────────────────────────────
        StackPane notifBell = buildNotifBell(stage);

        // ── ACTIVITY TOGGLE BUTTON ──────────────────────────────
        Button activityToggle = new Button("📋 Activity");
        UiStyles.applyButtonEffects(activityToggle, false);
        activityToggle.setOnAction(e -> {
            activityVisible = !activityVisible;
            activityPanel.setVisible(activityVisible);
            activityPanel.setManaged(activityVisible);
            if (activityVisible) {
                updateActivityFeed();
                FadeTransition ft = new FadeTransition(Duration.millis(200), activityPanel);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.play();
            }
            activityToggle.setText(activityVisible ? "📋 Hide Activity" : "📋 Activity");
        });

        // ── CLOCK ───────────────────────────────────────────────
        Label clockLabel = new Label();
        clockLabel.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-font-size: 12px;"
                        + "-fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";"
        );

        String activeUser = currentUser != null ? currentUser.getUsername() : "Guest";
        Label userLabel = new Label("👤 " + activeUser);
        userLabel.setStyle(UiStyles.LOGGED_IN_USER);

        Button usersButton = new Button("👥 Users");
        UiStyles.applyButtonEffects(usersButton, false);
        usersButton.setOnAction(e -> TaskDialogs.showUsersDirectoryDialog(assigneeNames, taskCache));

        Label title = new Label("Task Board");
        title.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-font-size: 22px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: " + UiStyles.COLOR_TEXT + ";"
        );

        Label subtitle = new Label("Collaborate · Plan · Deliver");
        subtitle.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-font-size: 12px;"
                        + "-fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";"
        );

        VBox titleBox = new VBox(2, title, subtitle);

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox topRow = new HBox(12, titleBox, topSpacer, activeUsersBox, activityToggle, notifBell, usersButton, userLabel, clockLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);

        HBox composeRow = new HBox(10, taskInput, priorityBox, dueDatePicker, assigneeBox, addButton, logoutButton);
        composeRow.setAlignment(Pos.CENTER_LEFT);
        composeRow.setPadding(new Insets(12, 0, 0, 0));

        VBox toolbar = new VBox(topRow, dashPanel, composeRow);
        toolbar.setPadding(new Insets(18, 20, 18, 20));
        toolbar.setStyle(
                "-fx-background-color: " + UiStyles.COLOR_SURFACE + ";"
                        + "-fx-background-radius: 12;"
                        + "-fx-border-color: " + UiStyles.COLOR_BORDER + ";"
                        + "-fx-border-radius: 12;"
                        + "-fx-border-width: 1;"
                        + "-fx-effect: dropshadow(gaussian, rgba(9,30,66,0.08), 12, 0, 0, 2);"
        );

        HBox columns = new HBox(16);
        columns.setAlignment(Pos.TOP_CENTER);
        columns.setPadding(new Insets(8, 0, 0, 0));
        columns.getChildren().addAll(
                kanbanTodo.getPanel(),
                kanbanDoing.getPanel(),
                kanbanDone.getPanel()
        );
        HBox.setHgrow(columns, Priority.ALWAYS);

        ScrollPane columnsScroll = new ScrollPane(columns);
        columnsScroll.setFitToHeight(true);
        columnsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        columnsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        columnsScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(columnsScroll, Priority.ALWAYS);

        VBox rootVbox = new VBox(12, toolbar, activityPanel, filterRow, columnsScroll);
        rootVbox.setPadding(new Insets(16));
        rootVbox.setStyle(UiStyles.BG_APP);

        mainScreenStack = new StackPane(rootVbox);

        Scene scene = UiWindowHelper.configureMainStage(stage, mainScreenStack);
        scene.getStylesheets().clear();
        UiStyles.applyGlobalStylesheet(scene);

        stage.setTitle("Task Manager — Kanban Board");

        Thread clockThread = new Thread(() -> {
            while (true) {
                try {
                    String time = java.time.LocalTime.now().withNano(0).toString();
                    Platform.runLater(() -> clockLabel.setText(time));
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });
        clockThread.setDaemon(true);
        clockThread.start();

        updateDashboard();
        stage.show();
    }

    public static void main(String[] args) {
        DatabaseConnection.connect();
        launch();
    }
}