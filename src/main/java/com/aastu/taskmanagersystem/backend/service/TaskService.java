package com.aastu.taskmanagersystem.backend.service;
import com.aastu.taskmanagersystem.backend.model.TaskEntity;
import com.aastu.taskmanagersystem.client.model.Task;
import com.aastu.taskmanagersystem.backend.model.CommentEntity;
import com.aastu.taskmanagersystem.backend.repository.TaskHistoryRepository;
import com.aastu.taskmanagersystem.backend.model.TaskHistoryEntity;
import com.aastu.taskmanagersystem.backend.repository.TaskRepository;
import com.aastu.taskmanagersystem.backend.repository.CommentRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository repository;

    @Autowired
    private TaskHistoryRepository historyRepository;

    @Autowired
    private CommentRepository commentRepository;

    public void logHistory(int taskId, String action, String username, String details) {
        try {
            TaskHistoryEntity entry = new TaskHistoryEntity();
            entry.setTaskId(taskId);
            entry.setAction(action);
            entry.setUsername(username == null || username.isBlank() ? "System" : username);
            entry.setDetails(details);
            entry.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            historyRepository.save(entry);
        } catch (Exception e) {
            System.err.println("Error saving task history: " + e.getMessage());
        }
    }

    public TaskEntity addTask(
            String title,
            String status,
            String priority,
            String dueDate,
            String assignedTo,
            String createdBy,
            String description,
            String checklist,
            int progress
    ) {
        TaskEntity task = new TaskEntity();
        task.setTitle(title);
        task.setStatus(status);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        task.setAssignedTo(assignedTo);
        task.setCreatedBy(createdBy);
        task.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        task.setDescription(description);
        task.setChecklist(checklist);
        task.setProgress(progress);

        TaskEntity saved = repository.save(task);
        logHistory(saved.getId(), "CREATED", createdBy, "Task created");
        return saved;
    }

    public void updateTaskById(
            int id,
            String title,
            String status,
            String priority,
            String dueDate,
            String assignedTo,
            String description,
            String checklist,
            int progress,
            String actor
    ) {
        Optional<TaskEntity> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return;
        }

        TaskEntity task = optional.get();

        if (!task.getTitle().equals(title)) {
            logHistory(id, "EDITED", actor, "Title changed from '" + task.getTitle() + "' to '" + title + "'");
        }
        if (!task.getStatus().equals(status)) {
            logHistory(id, "MOVED", actor, "Moved from " + task.getStatus() + " to " + status);
        }
        if (!task.getPriority().equals(priority)) {
            logHistory(id, "PRIORITY_CHANGED", actor, "Priority changed from " + task.getPriority() + " to " + priority);
        }
        if ((task.getAssignedTo() == null && assignedTo != null && !assignedTo.isBlank()) ||
                (task.getAssignedTo() != null && !task.getAssignedTo().equals(assignedTo))) {
            String oldAssignee = (task.getAssignedTo() == null || task.getAssignedTo().isBlank()) ? "Unassigned" : task.getAssignedTo();
            String newAssignee = (assignedTo == null || assignedTo.isBlank()) ? "Unassigned" : assignedTo;
            logHistory(id, "ASSIGNEE_CHANGED", actor, "Assignee changed from " + oldAssignee + " to " + newAssignee);
        }
        // Log generic edit if description or checklist changed
        boolean descChanged = (task.getDescription() == null && description != null && !description.isBlank()) ||
                (task.getDescription() != null && !task.getDescription().equals(description));
        boolean checklistChanged = (task.getChecklist() == null && checklist != null && !checklist.isBlank()) ||
                (task.getChecklist() != null && !task.getChecklist().equals(checklist));
        if (descChanged || checklistChanged) {
            logHistory(id, "EDITED", actor, "Task details updated (description/checklist)");
        }

        task.setTitle(title);
        task.setStatus(status);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        task.setAssignedTo(assignedTo);
        task.setDescription(description);
        task.setChecklist(checklist);
        task.setProgress(progress);

        repository.save(task);
        System.out.println("Updated task id=" + id + " title='" + title + "'");
    }

    public List<TaskEntity> getAllTasks() {
        return repository.findAll();
    }

    public void updateTask(
            String title,
            String status,
            String priority,
            String dueDate,
            String assignedTo,
            String createdBy,
            String description,
            String checklist,
            int progress,
            String actor
    ) {
        List<TaskEntity> allTasks = repository.findAll();
        for (TaskEntity task : allTasks) {
            if (task.getTitle().equals(title)) {
                updateTaskById(task.getId(), title, status, priority, dueDate, assignedTo, description, checklist, progress, actor);
                break;
            }
        }
    }

    public void deleteTask(String title, String actor) {
        List<TaskEntity> allTasks = repository.findAll();
        for (TaskEntity task : allTasks) {
            if (task.getTitle().equals(title)) {
                int id = task.getId();
                try {
                    // Clean up dependent tables
                    List<CommentEntity> comments = commentRepository.findByTaskId(id);
                    commentRepository.deleteAll(comments);
                    List<TaskHistoryEntity> history = historyRepository.findByTaskIdOrderByIdAsc(id);
                    historyRepository.deleteAll(history);
                } catch (Exception e) {
                    System.err.println("Error cleaning up comments/history on delete: " + e.getMessage());
                }
                repository.delete(task);
                break;
            }
        }
    }
}