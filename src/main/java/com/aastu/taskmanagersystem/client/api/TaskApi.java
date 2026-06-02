package com.aastu.taskmanagersystem.client.api;
import com.aastu.taskmanagersystem.client.model.Task;
import com.aastu.taskmanagersystem.client.model.TaskData;
import com.aastu.taskmanagersystem.backend.model.CommentEntity;
import com.aastu.taskmanagersystem.backend.model.TaskHistoryEntity;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class TaskApi {

    private static final String BASE_URL = "http://localhost:8080/tasks";
    private static final Gson gson = new Gson();

    // =========================
    // REUSABLE REQUEST METHOD
    // =========================
    private static void sendRequest(
            String endpoint,
            String method,
            String jsonBody
    ) {
        try {
            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            if (jsonBody != null) {
                OutputStream os = connection.getOutputStream();
                os.write(jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                os.flush();
                os.close();
            }
            connection.getResponseCode();
        } catch (Exception e) {
            System.out.println("API Request Error (" + method + " " + endpoint + "): " + e.getMessage());
        }
    }

    // =========================
    // ADD TASK
    // =========================
    public static void addTask(
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
        TaskData task = new TaskData();
        task.setTitle(title);
        task.setStatus(status);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        task.setAssignedTo(assignedTo);
        task.setCreatedBy(createdBy);
        task.setDescription(description);
        task.setChecklist(checklist);
        task.setProgress(progress);

        sendRequest("/add", "POST", gson.toJson(task));
    }

    // =========================
    // UPDATE TASK
    // =========================
    public static void updateTask(
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
        TaskData task = new TaskData();
        task.setTitle(title);
        task.setStatus(status);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        task.setAssignedTo(assignedTo);
        task.setCreatedBy(createdBy);
        task.setDescription(description);
        task.setChecklist(checklist);
        task.setProgress(progress);

        String query = "";
        if (actor != null && !actor.isBlank()) {
            try {
                query = "?actor=" + URLEncoder.encode(actor, "UTF-8");
            } catch (Exception ignored) {}
        }

        sendRequest("/update" + query, "PUT", gson.toJson(task));
    }

    public static void updateTaskById(
            int id,
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
        TaskData task = new TaskData();
        task.setId(id);
        task.setTitle(title);
        task.setStatus(status);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        task.setAssignedTo(assignedTo);
        task.setCreatedBy(createdBy);
        task.setDescription(description);
        task.setChecklist(checklist);
        task.setProgress(progress);

        String query = "";
        if (actor != null && !actor.isBlank()) {
            try {
                query = "?actor=" + URLEncoder.encode(actor, "UTF-8");
            } catch (Exception ignored) {}
        }

        sendRequest("/update" + query, "PUT", gson.toJson(task));
    }

    // =========================
    // DELETE TASK
    // =========================
    public static void deleteTask(String title, String actor) {
        TaskData task = new TaskData();
        task.setTitle(title);

        String query = "";
        if (actor != null && !actor.isBlank()) {
            try {
                query = "?actor=" + URLEncoder.encode(actor, "UTF-8");
            } catch (Exception ignored) {}
        }

        sendRequest("/delete" + query, "DELETE", gson.toJson(task));
    }

    // =========================
    // GET ALL TASKS
    // =========================
    public static List<TaskData> getAllTasks() {
        List<TaskData> tasks = new ArrayList<>();
        try {
            URL url = new URL(BASE_URL + "/all");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            tasks = gson.fromJson(
                    response.toString(),
                    new TypeToken<List<TaskData>>() {}.getType()
            );
        } catch (Exception e) {
            System.out.println("Error loading all tasks: " + e.getMessage());
        }
        return tasks;
    }

    // =========================
    // COMMENTS
    // =========================
    public static List<CommentEntity> getComments(int taskId) {
        List<CommentEntity> comments = new ArrayList<>();
        try {
            URL url = new URL(BASE_URL + "/comments?taskId=" + taskId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            comments = gson.fromJson(
                    response.toString(),
                    new TypeToken<List<CommentEntity>>() {}.getType()
            );
        } catch (Exception e) {
            System.out.println("Error fetching comments: " + e.getMessage());
        }
        return comments;
    }

    public static CommentEntity addComment(int taskId, String username, String text, String actor) {
        try {
            CommentEntity comment = new CommentEntity();
            comment.setTaskId(taskId);
            comment.setUsername(username);
            comment.setCommentText(text);

            String query = "";
            if (actor != null && !actor.isBlank()) {
                query = "?actor=" + URLEncoder.encode(actor, "UTF-8");
            }

            URL url = new URL(BASE_URL + "/comments" + query);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            OutputStream os = connection.getOutputStream();
            os.write(gson.toJson(comment).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            os.flush();
            os.close();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return gson.fromJson(response.toString(), CommentEntity.class);
        } catch (Exception e) {
            System.out.println("Error adding comment: " + e.getMessage());
            return null;
        }
    }

    public static void deleteComment(int commentId, String actor) {
        try {
            String query = "?id=" + commentId;
            if (actor != null && !actor.isBlank()) {
                query += "&actor=" + URLEncoder.encode(actor, "UTF-8");
            }

            URL url = new URL(BASE_URL + "/comments" + query);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.getResponseCode();
        } catch (Exception e) {
            System.out.println("Error deleting comment: " + e.getMessage());
        }
    }

    // =========================
    // TASK HISTORY (AUDIT LOGS)
    // =========================
    public static List<TaskHistoryEntity> getHistory(int taskId) {
        List<TaskHistoryEntity> history = new ArrayList<>();
        try {
            URL url = new URL(BASE_URL + "/history?taskId=" + taskId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            history = gson.fromJson(
                    response.toString(),
                    new TypeToken<List<TaskHistoryEntity>>() {}.getType()
            );
        } catch (Exception e) {
            System.out.println("Error fetching history: " + e.getMessage());
        }
        return history;
    }
}