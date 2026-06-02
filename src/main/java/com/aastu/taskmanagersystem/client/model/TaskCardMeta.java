package com.aastu.taskmanagersystem.client.model;

/**
 * Stored on each task Label via setUserData.
 */
public class TaskCardMeta {

    private final int id;
    private final String status;
    private final String priority;
    private final String dueDate;
    private final String assignedTo;
    private final String createdBy;
    private final String createdAt;

    public TaskCardMeta(
            int id,
            String status,
            String priority,
            String dueDate,
            String assignedTo,
            String createdBy,
            String createdAt
    ) {
        this.id = id;
        this.status = status == null ? "TODO" : status.trim();
        this.priority = priority == null ? "MEDIUM" : priority.trim();
        this.dueDate = dueDate == null ? "" : dueDate.trim();
        this.assignedTo = assignedTo == null ? "" : assignedTo.trim();
        this.createdBy = createdBy == null ? "" : createdBy.trim();
        this.createdAt = createdAt == null ? "" : createdAt.trim();
    }

    public static TaskCardMeta fromTaskItem(TaskItem item) {
        return new TaskCardMeta(
                item.getId(),
                item.getStatus(),
                item.getPriority(),
                item.getDueDate(),
                item.getAssignedTo(),
                item.getCreatedBy(),
                item.getCreatedAt()
        );
    }

    public static TaskCardMeta fromLabel(javafx.scene.control.Label label) {
        Object data = label.getUserData();
        if (data instanceof TaskCardMeta meta) {
            return meta;
        }
        return new TaskCardMeta(0, "TODO", "MEDIUM", "", "", "", "");
    }

    public int getId() { return id; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public String getDueDate() { return dueDate; }
    public String getAssignedTo() { return assignedTo; }
    public String getCreatedBy() { return createdBy; }
    public String getCreatedAt() { return createdAt; }
}
