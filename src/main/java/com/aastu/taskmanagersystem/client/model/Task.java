package com.aastu.taskmanagersystem.client.model;

public class Task {

    private String title;
    private String status;
    private String priority;
    private String dueDate;// NEW
    private String assignedTo;
    private String createdBy;

    public Task(
            String title,
            String status,
            String priority,
            String dueDate,
            String assignedTo
    ){
        this.title = title;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.assignedTo = assignedTo;
    }

    public Task(String title) {
        this.title = title;
        this.status = "TODO";
        this.priority = "MEDIUM";
    }

    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}