package com.aastu.taskmanagersystem.client.ui.dialogs;
import com.aastu.taskmanagersystem.client.ui.Main;
import com.aastu.taskmanagersystem.client.ui.styles.UiStyles;
import com.aastu.taskmanagersystem.client.model.Task;
import com.aastu.taskmanagersystem.backend.model.CommentEntity;
import com.aastu.taskmanagersystem.backend.model.TaskHistoryEntity;
import com.aastu.taskmanagersystem.client.api.TaskApi;
import com.aastu.taskmanagersystem.client.model.TaskItem;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

public final class TaskDialogs {

    private TaskDialogs() {}

    public static Optional<TaskItem> showEditDialog(
            TaskItem item,
            List<String> assigneeOptions
    ) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");
        dialog.setHeaderText("Update task details");
        dialog.initModality(Modality.APPLICATION_MODAL);

        TextField titleField = new TextField(item.getTitle());
        titleField.setStyle(UiStyles.inputField());
        titleField.setPrefWidth(240);
        UiStyles.applyFieldEffects(titleField);

        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("HIGH", "MEDIUM", "LOW");
        priorityBox.setValue(item.getPriority());
        priorityBox.setStyle(UiStyles.comboBox());
        priorityBox.setPrefWidth(240);
        UiStyles.applyFieldEffects(priorityBox);

        DatePicker duePicker = new DatePicker();
        if (item.getDueDate() != null && !item.getDueDate().isBlank()) {
            try {
                duePicker.setValue(java.time.LocalDate.parse(item.getDueDate()));
            } catch (Exception ignored) {
            }
        }
        duePicker.setStyle(UiStyles.comboBox());
        duePicker.setPrefWidth(240);
        UiStyles.applyFieldEffects(duePicker);

        ComboBox<String> assigneeBox = new ComboBox<>();
        assigneeBox.getItems().addAll(assigneeOptions);
        if (item.getAssignedTo() != null && !item.getAssignedTo().isBlank()) {
            assigneeBox.setValue(item.getAssignedTo());
        } else if (!assigneeOptions.isEmpty()) {
            assigneeBox.setValue(assigneeOptions.get(0));
        }
        assigneeBox.setStyle(UiStyles.comboBox());
        assigneeBox.setPrefWidth(240);
        UiStyles.applyFieldEffects(assigneeBox);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setPadding(new Insets(20));

        Label titleLabel = new Label("Title");
        titleLabel.setStyle(UiStyles.FONT_FAMILY + "-fx-font-weight: bold; -fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + "; -fx-font-size: 12px;");
        Label priorityLabel = new Label("Priority");
        priorityLabel.setStyle(UiStyles.FONT_FAMILY + "-fx-font-weight: bold; -fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + "; -fx-font-size: 12px;");
        Label dueLabel = new Label("Due date");
        dueLabel.setStyle(UiStyles.FONT_FAMILY + "-fx-font-weight: bold; -fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + "; -fx-font-size: 12px;");
        Label assigneeLabel = new Label("Assignee");
        assigneeLabel.setStyle(UiStyles.FONT_FAMILY + "-fx-font-weight: bold; -fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + "; -fx-font-size: 12px;");

        grid.add(titleLabel, 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(priorityLabel, 0, 1);
        grid.add(priorityBox, 1, 1);
        grid.add(dueLabel, 0, 2);
        grid.add(duePicker, 1, 2);
        grid.add(assigneeLabel, 0, 3);
        grid.add(assigneeBox, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (okButton != null) {
            UiStyles.applyButtonEffects(okButton, true);
        }
        if (cancelButton != null) {
            UiStyles.applyButtonEffects(cancelButton, false);
        }

        dialog.setOnShowing(ev -> {
            Scene dialogScene = dialog.getDialogPane().getScene();
            if (dialogScene != null) {
                UiStyles.applyGlobalStylesheet(dialogScene);
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return Optional.empty();
        }

        String newTitle = titleField.getText().trim();
        if (newTitle.isEmpty()) {
            return Optional.empty();
        }

        TaskItem updated = new TaskItem();
        updated.setId(item.getId());
        updated.setTitle(newTitle);
        updated.setStatus(item.getStatus());
        updated.setPriority(priorityBox.getValue());
        updated.setDueDate(
                duePicker.getValue() != null
                        ? duePicker.getValue().toString()
                        : ""
        );
        updated.setAssignedTo(
                assigneeBox.getValue() != null ? assigneeBox.getValue() : ""
        );
        updated.setCreatedBy(item.getCreatedBy());
        updated.setCreatedAt(item.getCreatedAt());
        return Optional.of(updated);
    }

    public static Integer activeDetailsTaskId = null;
    public static Runnable commentsRefresher = null;
    public static Runnable historyRefresher = null;
    public static Runnable detailsRefresher = null;
    public static Label activeTypingLabel = null;
    private static final Gson gson = new Gson();

    public static class ChecklistItem {
        public String text;
        public boolean done;
        public ChecklistItem(String text, boolean done) {
            this.text = text;
            this.done = done;
        }
    }

    private static List<ChecklistItem> parseChecklist(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return gson.fromJson(json, new TypeToken<List<ChecklistItem>>(){}.getType());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static void saveAndBroadcastChecklist(TaskItem item, List<ChecklistItem> items, String actor, PrintWriter writer, java.util.function.Consumer<TaskItem> onUpdate) {
        String json = gson.toJson(items);
        item.setChecklist(json);
        int total = items.size();
        long done = items.stream().filter(i -> i.done).count();
        int progress = total == 0 ? 0 : (int) (done * 100 / total);
        item.setProgress(progress);

        TaskApi.updateTaskById(
                item.getId(),
                item.getTitle(),
                item.getStatus(),
                item.getPriority(),
                item.getDueDate(),
                item.getAssignedTo(),
                item.getCreatedBy(),
                item.getDescription(),
                item.getChecklist(),
                item.getProgress(),
                actor
        );

        if (writer != null) {
            writer.println("EDIT|" + item.getId() + "|" + item.getTitle() + "|" + item.getStatus() + "|" + item.getPriority() + "|" + item.getDueDate() + "|" + item.getAssignedTo() + "|" + item.getCreatedBy() + "|" + item.getCreatedAt() + "|" + actor);
        }

        if (onUpdate != null) {
            onUpdate.accept(item);
        }
    }

    public static void showDetailsDialog(TaskItem item, String currentUser, PrintWriter writer, java.util.function.Consumer<TaskItem> onUpdate) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Task Details — " + item.getTitle());
        stage.setResizable(false);

        activeDetailsTaskId = item.getId();

        // Broadcast editing start
        if (writer != null) {
            writer.println("TYPING:EDIT_START:" + currentUser + ":" + item.getId());
        }

        stage.setOnCloseRequest(e -> {
            activeDetailsTaskId = null;
            commentsRefresher = null;
            historyRefresher = null;
            detailsRefresher = null;
            activeTypingLabel = null;
            if (writer != null) {
                writer.println("TYPING:EDIT_END:" + currentUser + ":" + item.getId());
                writer.println("TYPING:COMMENT_END:" + currentUser + ":" + item.getId());
            }
        });

        HBox mainLayout = new HBox(24);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: " + UiStyles.COLOR_SURFACE + ";");
        mainLayout.setPrefSize(850, 600);

        // ==========================================
        // LEFT COLUMN: metadata, desc, checklist
        // ==========================================
        VBox leftCol = new VBox(14);
        leftCol.setPrefWidth(440);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        // Header Row (Icon + Title)
        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Label iconLbl = new Label("📌");
        iconLbl.setStyle("-fx-font-size: 18px;");
        Label titleLbl = new Label(item.getTitle());
        titleLbl.setWrapText(true);
        titleLbl.setMaxWidth(400);
        titleLbl.setStyle(UiStyles.FONT_FAMILY + "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + UiStyles.COLOR_TEXT + ";");
        headerRow.getChildren().addAll(iconLbl, titleLbl);

        // Badges grid
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(10);
        grid.setPadding(new Insets(5, 0, 5, 0));

        // Creator badge
        String creator = item.getCreatedBy() == null || item.getCreatedBy().isBlank() ? "System" : item.getCreatedBy();
        Label creatorBadge = new Label("✍️ " + creator);
        creatorBadge.setStyle(UiStyles.BADGE_CREATOR);

        // Assignee badge
        String assignee = item.getAssignedTo() == null || item.getAssignedTo().isBlank() ? "Unassigned" : item.getAssignedTo();
        Label assigneeBadge = new Label("👤 " + assignee);
        assigneeBadge.setStyle(UiStyles.BADGE_ASSIGNEE);

        // Status badge
        String status = item.getStatus();
        String statusColor = "#0052CC";
        if ("DOING".equals(status)) statusColor = "#FF991F";
        else if ("DONE".equals(status)) statusColor = "#00875A";
        Label statusBadge = new Label(status);
        statusBadge.setStyle(UiStyles.FONT_FAMILY + "-fx-background-color: " + statusColor + "15; -fx-text-fill: " + statusColor + "; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 3 8; -fx-background-radius: 4;");

        // Priority badge
        String priority = item.getPriority();
        String priorityColor = "#FF991F";
        if ("HIGH".equals(priority)) priorityColor = "#DE350B";
        else if ("LOW".equals(priority)) priorityColor = "#00875A";
        Label priorityBadge = new Label(priority);
        priorityBadge.setStyle(UiStyles.FONT_FAMILY + "-fx-background-color: " + priorityColor + "15; -fx-text-fill: " + priorityColor + "; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 3 8; -fx-background-radius: 4;");

        // Due date badge
        Label dueBadge = new Label();
        if (item.getDueDate() == null || item.getDueDate().isBlank()) {
            dueBadge.setText("No due date");
            dueBadge.setStyle(UiStyles.FONT_FAMILY + "-fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + "; -fx-font-size: 12px;");
        } else {
            boolean overdue = false;
            try {
                java.time.LocalDate due = java.time.LocalDate.parse(item.getDueDate());
                overdue = due.isBefore(java.time.LocalDate.now());
            } catch (Exception ignored) {}
            if (overdue) {
                dueBadge.setText("⚠️ Overdue (" + item.getDueDate() + ")");
                dueBadge.setStyle(UiStyles.BADGE_OVERDUE);
            } else {
                dueBadge.setText("📅 " + item.getDueDate());
                dueBadge.setStyle(UiStyles.FONT_FAMILY + "-fx-background-color: #FAFBFC; -fx-text-fill: " + UiStyles.COLOR_TEXT + "; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 3 8; -fx-background-radius: 4; -fx-border-color: " + UiStyles.COLOR_BORDER + "; -fx-border-radius: 4;");
            }
        }

        // Add to grid
        addDetailsGridRow(grid, 0, "✍️ Creator", creatorBadge);
        addDetailsGridRow(grid, 1, "👤 Assignee", assigneeBadge);
        addDetailsGridRow(grid, 2, "📋 Status", statusBadge);
        addDetailsGridRow(grid, 3, "🔥 Priority", priorityBadge);
        addDetailsGridRow(grid, 4, "📅 Due Date", dueBadge);

        // Description Section
        VBox descBox = new VBox(6);
        Label descTitle = new Label("📝 Description");
        descTitle.setStyle(UiStyles.FONT_FAMILY + "-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + UiStyles.COLOR_TEXT + ";");
        TextArea descInput = new TextArea(item.getDescription());
        descInput.setPromptText("Add a detailed description...");
        descInput.setWrapText(true);
        descInput.setPrefHeight(90);
        descInput.setStyle(UiStyles.inputField());

        Button saveDescBtn = new Button("Save description");
        saveDescBtn.setStyle(UiStyles.primaryButton() + "-fx-padding: 5 12;");
        saveDescBtn.setVisible(false);

        descInput.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                saveDescBtn.setVisible(true);
            }
        });

        saveDescBtn.setOnAction(e -> {
            String newDesc = descInput.getText().trim();
            item.setDescription(newDesc);
            TaskApi.updateTaskById(
                    item.getId(),
                    item.getTitle(),
                    item.getStatus(),
                    item.getPriority(),
                    item.getDueDate(),
                    item.getAssignedTo(),
                    item.getCreatedBy(),
                    newDesc,
                    item.getChecklist(),
                    item.getProgress(),
                    currentUser
            );
            if (writer != null) {
                writer.println("EDIT|" + item.getId() + "|" + item.getTitle() + "|" + item.getStatus() + "|" + item.getPriority() + "|" + item.getDueDate() + "|" + item.getAssignedTo() + "|" + item.getCreatedBy() + "|" + item.getCreatedAt() + "|" + currentUser);
            }
            saveDescBtn.setVisible(false);
            if (onUpdate != null) {
                onUpdate.accept(item);
            }
        });

        HBox descActions = new HBox(saveDescBtn);
        descActions.setAlignment(Pos.CENTER_LEFT);
        descBox.getChildren().addAll(descTitle, descInput, descActions);

        // Checklist Section
        VBox checklistBox = new VBox(6);
        Label checklistTitle = new Label("📋 Subtasks / Checklist");
        checklistTitle.setStyle(UiStyles.FONT_FAMILY + "-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + UiStyles.COLOR_TEXT + ";");

        ProgressBar progressBar = new ProgressBar(0.0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: #36B37E;"); // Custom green accent

        Label progressText = new Label("0%");
        progressText.setStyle(UiStyles.FONT_FAMILY + "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";");

        HBox progressRow = new HBox(8, progressBar, progressText);
        progressRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        VBox checklistItemsBox = new VBox(6);
        ScrollPane checklistScroll = new ScrollPane(checklistItemsBox);
        checklistScroll.setFitToWidth(true);
        checklistScroll.setPrefHeight(120);
        checklistScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        Runnable rebuildChecklist = new Runnable() {
            @Override
            public void run() {
                checklistItemsBox.getChildren().clear();
                List<ChecklistItem> items = parseChecklist(item.getChecklist());
                int total = items.size();
                long done = items.stream().filter(i -> i.done).count();
                double progressVal = total == 0 ? 0.0 : (double) done / total;
                progressBar.setProgress(progressVal);
                progressText.setText((int)(progressVal * 100) + "%");

                for (int i = 0; i < items.size(); i++) {
                    final int idx = i;
                    ChecklistItem cli = items.get(i);
                    CheckBox cb = new CheckBox(cli.text);
                    cb.setSelected(cli.done);
                    cb.setStyle(UiStyles.FONT_FAMILY + "-fx-font-size: 12px; -fx-text-fill: " + UiStyles.COLOR_TEXT + ";");

                    cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                        cli.done = newVal;
                        saveAndBroadcastChecklist(item, items, currentUser, writer, onUpdate);
                        run();
                    });

                    Button delBtn = new Button("🗑️");
                    delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #DE350B; -fx-cursor: hand; -fx-padding: 0 4;");
                    delBtn.setOnAction(e -> {
                        items.remove(idx);
                        saveAndBroadcastChecklist(item, items, currentUser, writer, onUpdate);
                        run();
                    });

                    HBox itemRow = new HBox(8, cb, new Region(), delBtn);
                    HBox.setHgrow(itemRow.getChildren().get(1), Priority.ALWAYS);
                    itemRow.setAlignment(Pos.CENTER_LEFT);
                    checklistItemsBox.getChildren().add(itemRow);
                }
            }
        };

        // Add subtask input row
        TextField newSubtaskField = new TextField();
        newSubtaskField.setPromptText("Add a subtask...");
        UiStyles.applyFieldEffects(newSubtaskField);
        HBox.setHgrow(newSubtaskField, Priority.ALWAYS);

        Button addSubtaskBtn = new Button("Add");
        addSubtaskBtn.setStyle(UiStyles.primaryButton() + "-fx-padding: 6 14;");
        addSubtaskBtn.setOnAction(e -> {
            String text = newSubtaskField.getText().trim();
            if (!text.isEmpty()) {
                List<ChecklistItem> items = parseChecklist(item.getChecklist());
                items.add(new ChecklistItem(text, false));
                saveAndBroadcastChecklist(item, items, currentUser, writer, onUpdate);
                newSubtaskField.clear();
                rebuildChecklist.run();
            }
        });

        HBox addSubtaskRow = new HBox(8, newSubtaskField, addSubtaskBtn);
        addSubtaskRow.setAlignment(Pos.CENTER_LEFT);

        checklistBox.getChildren().addAll(checklistTitle, progressRow, checklistScroll, addSubtaskRow);

        leftCol.getChildren().addAll(headerRow, grid, descBox, checklistBox);

        // ==========================================
        // RIGHT COLUMN: Comments & History TabPane
        // ==========================================
        VBox rightCol = new VBox(10);
        rightCol.setPrefWidth(350);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        HBox.setHgrow(tabPane, Priority.ALWAYS);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // Comments Tab
        Tab commentsTab = new Tab("Comments");
        VBox commentTabContent = new VBox(8);
        commentTabContent.setPadding(new Insets(10, 0, 10, 0));

        VBox commentsBox = new VBox(8);
        ScrollPane commentsScroll = new ScrollPane(commentsBox);
        commentsScroll.setFitToWidth(true);
        commentsScroll.setPrefHeight(340);
        commentsScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        Label typingLabel = new Label();
        typingLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + "; -fx-font-style: italic;");
        activeTypingLabel = typingLabel;

        Runnable rebuildComments = new Runnable() {
            @Override
            public void run() {
                commentsBox.getChildren().clear();
                List<CommentEntity> comments = TaskApi.getComments(item.getId());
                for (CommentEntity c : comments) {
                    HBox card = new HBox(8);
                    card.setPadding(new Insets(8));
                    card.setStyle(UiStyles.CHAT_BUBBLE);
                    card.setAlignment(Pos.TOP_LEFT);

                    Label avatar = UiStyles.createUserAvatar(c.getUsername(), "#0052CC");

                    VBox textContent = new VBox(2);
                    HBox meta = new HBox(6);
                    meta.setAlignment(Pos.CENTER_LEFT);

                    Label nameLbl = new Label(c.getUsername());
                    nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: " + UiStyles.COLOR_TEXT + ";");

                    Label timeLbl = new Label(c.getCreatedAt());
                    timeLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";");

                    meta.getChildren().addAll(nameLbl, timeLbl);

                    Label textLbl = new Label(c.getCommentText());
                    textLbl.setWrapText(true);
                    textLbl.setMaxWidth(240);
                    textLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + UiStyles.COLOR_TEXT + ";");

                    textContent.getChildren().addAll(meta, textLbl);
                    HBox.setHgrow(textContent, Priority.ALWAYS);

                    card.getChildren().addAll(avatar, textContent);

                    if (currentUser.equalsIgnoreCase(c.getUsername().trim())) {
                        Button delCommentBtn = new Button("❌");
                        delCommentBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #DE350B; -fx-cursor: hand; -fx-padding: 0 4;");
                        delCommentBtn.setOnAction(e -> {
                            TaskApi.deleteComment(c.getId(), currentUser);
                            if (writer != null) {
                                writer.println("COMMENT:DELETE:" + item.getId() + ":" + c.getId() + ":" + currentUser);
                            }
                            run();
                            if (historyRefresher != null) {
                                historyRefresher.run();
                            }
                        });
                        card.getChildren().add(delCommentBtn);
                    }

                    commentsBox.getChildren().add(card);
                }
            }
        };
        commentsRefresher = rebuildComments;

        // Add Comment input row
        TextArea newCommentInput = new TextArea();
        newCommentInput.setPromptText("Write a comment...");
        newCommentInput.setWrapText(true);
        newCommentInput.setPrefHeight(50);
        newCommentInput.setStyle(UiStyles.inputField());

        Button addCommentBtn = new Button("Send");
        addCommentBtn.setStyle(UiStyles.primaryButton() + "-fx-padding: 6 12;");
        addCommentBtn.setOnAction(e -> {
            String text = newCommentInput.getText().trim();
            if (!text.isEmpty()) {
                CommentEntity comment = TaskApi.addComment(item.getId(), currentUser, text, currentUser);
                if (comment != null) {
                    newCommentInput.clear();
                    if (writer != null) {
                        writer.println("COMMENT:ADD:" + item.getId() + ":" + comment.getId() + ":" + currentUser);
                    }
                    rebuildComments.run();
                    if (historyRefresher != null) {
                        historyRefresher.run();
                    }
                }
            }
        });

        // Typing timer setup
        PauseTransition typingTimer = new PauseTransition(Duration.seconds(2.0));
        typingTimer.setOnFinished(ev -> {
            if (writer != null) {
                writer.println("TYPING:COMMENT_END:" + currentUser + ":" + item.getId());
            }
        });

        newCommentInput.setOnKeyTyped(ev -> {
            if (writer != null) {
                writer.println("TYPING:COMMENT_START:" + currentUser + ":" + item.getId());
            }
            typingTimer.playFromStart();
        });

        HBox commentInputRow = new HBox(8, newCommentInput, addCommentBtn);
        commentInputRow.setAlignment(Pos.BOTTOM_LEFT);
        HBox.setHgrow(newCommentInput, Priority.ALWAYS);

        commentTabContent.getChildren().addAll(commentsScroll, typingLabel, commentInputRow);
        commentsTab.setContent(commentTabContent);

        // History Tab
        Tab historyTab = new Tab("History / Audit");
        VBox historyTabContent = new VBox(6);
        historyTabContent.setPadding(new Insets(10, 0, 10, 0));

        VBox historyBox = new VBox(6);
        ScrollPane historyScroll = new ScrollPane(historyBox);
        historyScroll.setFitToWidth(true);
        historyScroll.setPrefHeight(420);
        historyScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        Runnable rebuildHistory = new Runnable() {
            @Override
            public void run() {
                historyBox.getChildren().clear();
                List<TaskHistoryEntity> historyList = TaskApi.getHistory(item.getId());
                for (TaskHistoryEntity h : historyList) {
                    VBox row = new VBox(2);
                    row.setStyle(UiStyles.HISTORY_ENTRY);

                    Label hHeader = new Label("👤 " + h.getUsername() + "  " + h.getAction());
                    hHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: " + UiStyles.COLOR_TEXT + ";");

                    Label hDetails = new Label(h.getDetails());
                    hDetails.setWrapText(true);
                    hDetails.setMaxWidth(310);
                    hDetails.setStyle("-fx-font-size: 11px; -fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";");

                    Label hTime = new Label(h.getCreatedAt());
                    hTime.setStyle("-fx-font-size: 9px; -fx-text-fill: #7A869A;");

                    row.getChildren().addAll(hHeader, hDetails, hTime);
                    historyBox.getChildren().add(row);
                }
            }
        };
        historyRefresher = rebuildHistory;

        historyTabContent.getChildren().add(historyScroll);
        historyTab.setContent(historyTabContent);

        tabPane.getTabs().addAll(commentsTab, historyTab);
        rightCol.getChildren().add(tabPane);

        detailsRefresher = () -> {
            javafx.application.Platform.runLater(() -> {
                descInput.setText(item.getDescription());
                rebuildChecklist.run();
                rebuildComments.run();
                rebuildHistory.run();
            });
        };

        // Run initial loads
        rebuildChecklist.run();
        rebuildComments.run();
        rebuildHistory.run();

        mainLayout.getChildren().addAll(leftCol, rightCol);

        Scene scene = new Scene(mainLayout);
        UiStyles.applyGlobalStylesheet(scene);
        stage.setScene(scene);

        // Smooth Fade & Scale Transition on open
        mainLayout.setOpacity(0);
        mainLayout.setScaleX(0.95);
        mainLayout.setScaleY(0.95);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), mainLayout);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        javafx.animation.ScaleTransition scaleIn = new javafx.animation.ScaleTransition(Duration.millis(250), mainLayout);
        scaleIn.setFromX(0.95);
        scaleIn.setFromY(0.95);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        fadeIn.play();
        scaleIn.play();

        stage.showAndWait();
    }

    private static void addDetailsGridRow(GridPane grid, int row, String labelText, javafx.scene.Node valueNode) {
        Label label = new Label(labelText);
        label.setStyle(
                UiStyles.FONT_FAMILY
                + "-fx-font-size: 12px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";"
        );
        grid.add(label, 0, row);
        grid.add(valueNode, 1, row);
    }

    public static void showUsersDirectoryDialog(List<String> usernames, List<TaskItem> allTasks) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Users Directory");
        dialog.setHeaderText("Workspace Collaboration Directory");
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox container = new VBox(12);
        container.setPadding(new Insets(14));
        container.setMinWidth(420);
        container.setPrefWidth(460);

        for (String username : usernames) {
            final String u = username.trim();
            long created = allTasks.stream()
                    .filter(t -> t.getCreatedBy() != null && u.equalsIgnoreCase(t.getCreatedBy().trim()))
                    .count();
            long assigned = allTasks.stream()
                    .filter(t -> t.getAssignedTo() != null && u.equalsIgnoreCase(t.getAssignedTo().trim()))
                    .count();
            long completed = allTasks.stream()
                    .filter(t -> t.getAssignedTo() != null && u.equalsIgnoreCase(t.getAssignedTo().trim()) && "DONE".equalsIgnoreCase(t.getStatus()))
                    .count();

            // Row Container
            HBox userRow = new HBox(12);
            userRow.setAlignment(Pos.CENTER_LEFT);
            userRow.setPadding(new Insets(10, 14, 10, 14));
            userRow.setStyle(
                    "-fx-background-color: " + UiStyles.COLOR_SURFACE + ";"
                    + "-fx-border-color: " + UiStyles.COLOR_BORDER + ";"
                    + "-fx-border-radius: 8;"
                    + "-fx-background-radius: 8;"
                    + "-fx-effect: dropshadow(gaussian, rgba(9,30,66,0.04), 4, 0, 0, 1);"
            );

            boolean isOnline = Main.onlineUsers.stream().anyMatch(name -> name.equalsIgnoreCase(u));
            String lastSeen = Main.lastSeenMap.getOrDefault(u.toLowerCase(), "recently");

            // User Avatar
            Label avatar = UiStyles.createUserAvatar(u, isOnline ? "#36B37E" : "#5243AA");

            // Details VBox
            VBox details = new VBox(6);

            HBox nameRow = new HBox(8);
            nameRow.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label(u);
            nameLabel.setStyle(
                    UiStyles.FONT_FAMILY
                    + "-fx-font-size: 14px;"
                    + "-fx-font-weight: bold;"
                    + "-fx-text-fill: " + UiStyles.COLOR_TEXT + ";"
            );

            Region dot = new Region();
            dot.setStyle(isOnline ? UiStyles.DOT_ONLINE : UiStyles.DOT_OFFLINE);

            Label presenceLbl = new Label(isOnline ? "Online" : "Last seen: " + lastSeen);
            presenceLbl.setStyle(
                    UiStyles.FONT_FAMILY
                    + "-fx-font-size: 11px;"
                    + "-fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";"
                    + "-fx-font-style: italic;"
            );

            nameRow.getChildren().addAll(nameLabel, dot, presenceLbl);

            HBox statsRow = new HBox(8);
            statsRow.setAlignment(Pos.CENTER_LEFT);

            Label createdBadge = new Label("✍️ Created: " + created);
            createdBadge.setStyle(
                    UiStyles.FONT_FAMILY
                    + "-fx-background-color: #E2F8F0;"
                    + "-fx-text-fill: #00875A;"
                    + "-fx-font-size: 10px;"
                    + "-fx-font-weight: bold;"
                    + "-fx-padding: 2 6;"
                    + "-fx-background-radius: 4;"
            );

            Label assignedBadge = new Label("👤 Assigned: " + assigned);
            assignedBadge.setStyle(
                    UiStyles.FONT_FAMILY
                    + "-fx-background-color: #E2EEFF;"
                    + "-fx-text-fill: #0052CC;"
                    + "-fx-font-size: 10px;"
                    + "-fx-font-weight: bold;"
                    + "-fx-padding: 2 6;"
                    + "-fx-background-radius: 4;"
            );

            Label completedBadge = new Label("✅ Done: " + completed);
            completedBadge.setStyle(
                    UiStyles.FONT_FAMILY
                    + "-fx-background-color: #E3FCEF;"
                    + "-fx-text-fill: #006644;"
                    + "-fx-font-size: 10px;"
                    + "-fx-font-weight: bold;"
                    + "-fx-padding: 2 6;"
                    + "-fx-background-radius: 4;"
            );

            statsRow.getChildren().addAll(createdBadge, assignedBadge, completedBadge);
            details.getChildren().addAll(nameRow, statsRow);

            userRow.getChildren().addAll(avatar, details);
            container.getChildren().add(userRow);
        }

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPrefHeight(360);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        Button closeBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        if (closeBtn != null) {
            UiStyles.applyButtonEffects(closeBtn, false);
        }

        dialog.getDialogPane().setStyle(
                UiStyles.FONT_FAMILY
                + "-fx-background-color: " + UiStyles.COLOR_SURFACE + ";"
                + "-fx-border-radius: 12;"
                + "-fx-background-radius: 12;"
        );

        dialog.setOnShowing(ev -> {
            Scene dialogScene = dialog.getDialogPane().getScene();
            if (dialogScene != null) {
                UiStyles.applyGlobalStylesheet(dialogScene);
            }
        });

        dialog.showAndWait();
    }
}

