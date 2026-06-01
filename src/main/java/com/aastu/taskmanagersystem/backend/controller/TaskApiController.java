package com.aastu.taskmanagersystem.backend.controller;
import com.aastu.taskmanagersystem.backend.model.TaskEntity;
import com.aastu.taskmanagersystem.backend.service.TaskService;
import com.aastu.taskmanagersystem.client.model.Task;
import com.aastu.taskmanagersystem.backend.model.CommentEntity;
import com.aastu.taskmanagersystem.backend.repository.TaskHistoryRepository;
import com.aastu.taskmanagersystem.backend.model.TaskHistoryEntity;
import com.aastu.taskmanagersystem.backend.repository.CommentRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tasks")
public class TaskApiController {

    @Autowired
    private TaskService service;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TaskHistoryRepository historyRepository;

    @PostMapping("/add")
    public String addTask(
            @RequestBody TaskEntity task
    ) {
        service.addTask(
                task.getTitle(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getAssignedTo(),
                task.getCreatedBy(),
                task.getDescription(),
                task.getChecklist(),
                task.getProgress()
        );
        return "Task Added";
    }

    @PutMapping("/update")
    public String updateTask(
            @RequestBody TaskEntity task,
            @RequestParam(value = "actor", required = false) String actor
    ) {
        String act = (actor == null || actor.isBlank()) ? task.getCreatedBy() : actor;
        if (task.getId() > 0) {
            service.updateTaskById(
                    task.getId(),
                    task.getTitle(),
                    task.getStatus(),
                    task.getPriority(),
                    task.getDueDate(),
                    task.getAssignedTo(),
                    task.getDescription(),
                    task.getChecklist(),
                    task.getProgress(),
                    act
            );
        } else {
            service.updateTask(
                    task.getTitle(),
                    task.getStatus(),
                    task.getPriority(),
                    task.getDueDate(),
                    task.getAssignedTo(),
                    task.getCreatedBy(),
                    task.getDescription(),
                    task.getChecklist(),
                    task.getProgress(),
                    act
            );
        }
        return "Task Updated";
    }

    @DeleteMapping("/delete")
    public String deleteTask(
            @RequestBody TaskEntity task,
            @RequestParam(value = "actor", required = false) String actor
    ) {
        service.deleteTask(task.getTitle(), actor);
        return "Task Deleted";
    }

    @GetMapping("/all")
    public List<TaskEntity> getAllTasks() {
        return service.getAllTasks();
    }

    // ── COMMENTS ENDPOINTS ────────────────────────────────────

    @GetMapping("/comments")
    public List<CommentEntity> getComments(@RequestParam("taskId") int taskId) {
        return commentRepository.findByTaskId(taskId);
    }

    @PostMapping("/comments")
    public CommentEntity addComment(
            @RequestBody CommentEntity comment,
            @RequestParam(value = "actor", required = false) String actor
    ) {
        comment.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        CommentEntity saved = commentRepository.save(comment);
        String act = (actor == null || actor.isBlank()) ? comment.getUsername() : actor;
        service.logHistory(comment.getTaskId(), "COMMENT_ADDED", act, "Added comment: \"" + comment.getCommentText() + "\"");
        return saved;
    }

    @DeleteMapping("/comments")
    public String deleteComment(
            @RequestParam("id") int id,
            @RequestParam(value = "actor", required = false) String actor
    ) {
        Optional<CommentEntity> opt = commentRepository.findById(id);
        if (opt.isPresent()) {
            CommentEntity comment = opt.get();
            commentRepository.delete(comment);
            service.logHistory(comment.getTaskId(), "COMMENT_DELETED", actor, "Deleted comment by " + comment.getUsername());
            return "Comment Deleted";
        }
        return "Comment not found";
    }

    // ── AUDIT LOG / HISTORY ENDPOINTS ─────────────────────────

    @GetMapping("/history")
    public List<TaskHistoryEntity> getHistory(@RequestParam("taskId") int taskId) {
        return historyRepository.findByTaskIdOrderByIdAsc(taskId);
    }
}
