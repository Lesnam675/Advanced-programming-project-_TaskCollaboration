package com.aastu.taskmanagersystem.client.model;

/**
 * In-memory task model for board, filters, and sorting.
 */
public class TaskItem {

    private int id;
    private String title;
    private String status;
    private String priority;
    private String dueDate;
    private String assignedTo;
    private String createdBy;
    private String createdAt;
    private String updatedAt;
    private String description;
    private String checklist;
    private int progress;

    public static TaskItem fromData(TaskData data) {
        TaskItem item = new TaskItem();
        item.id = data.getId();
        item.title = data.getTitle() == null ? "" : data.getTitle();
        item.status = data.getStatus() == null ? "TODO" : data.getStatus();
        item.priority = data.getPriority() == null ? "MEDIUM" : data.getPriority();
        item.dueDate = data.getDueDate() == null ? "" : data.getDueDate();
        item.assignedTo = data.getAssignedTo() == null ? "" : data.getAssignedTo();
        item.createdBy = data.getCreatedBy() == null ? "" : data.getCreatedBy();
        item.createdAt = data.getCreatedAt() == null ? "" : data.getCreatedAt();
        item.updatedAt = "";
        item.description = data.getDescription() == null ? "" : data.getDescription();
        item.checklist = data.getChecklist() == null ? "" : data.getChecklist();
        item.progress = data.getProgress();
        return item;
    }

    public Task toTask() {
        Task task = new Task(title, status, priority, dueDate, assignedTo);
        task.setCreatedBy(createdBy);
        return task;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getChecklist() { return checklist; }
    public void setChecklist(String checklist) { this.checklist = checklist; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
}

