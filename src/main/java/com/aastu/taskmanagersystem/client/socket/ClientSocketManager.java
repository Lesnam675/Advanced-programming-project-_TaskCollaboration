package com.aastu.taskmanagersystem.client.socket;
import com.aastu.taskmanagersystem.client.model.Task;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javafx.application.Platform;

public class ClientSocketManager {

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private final SocketCallback callback;

    public interface SocketCallback {
        void onNewTask(String title, String priority, String dueDate, String assignedTo, String createdBy);
        void onMoveTask(String title, String status, String priority);
        void onEditTask(int id, String title, String status, String priority, String dueDate, String assignee, String creator, String createdAt, String actor);
        void onDeleteTask(String title);
        void onPresenceOnline(String username);
        void onPresenceOffline(String username);
        void onPresenceList(String usersList);
        void onTyping(String action, String user, int taskId);
        void onCommentSync(int taskId);
    }

    public ClientSocketManager(String host, int port, SocketCallback callback) {
        this.callback = callback;
        try {
            this.socket = new Socket(host, port);
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("[SOCKET] Connected to real-time server!");
        } catch (Exception e) {
            System.err.println("[SOCKET] Error establishing server connection: " + e.getMessage());
        }
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void startListener() {
        if (reader == null) return;
        Thread listenThread = new Thread(() -> {
            try {
                while (true) {
                    String message = reader.readLine();
                    if (message == null) break;

                    // ── NEW TASK ──
                    if (message.startsWith("NEW TASK:")) {
                        String data = message.replace("NEW TASK:", "");
                        String[] parts = data.split(":");
                        String taskText  = parts[0];
                        String priority  = parts[1];
                        String dueDate   = parts[2];
                        String assignedTo = parts[3];
                        String createdBy = parts[4];
                        Platform.runLater(() -> callback.onNewTask(taskText, priority, dueDate, assignedTo, createdBy));
                    }

                    // ── MOVE TASK ──
                    else if (message.startsWith("MOVE:")) {
                        String data = message.replace("MOVE:", "");
                        String[] parts = data.split(":");
                        String title    = parts[0];
                        String status   = parts[1];
                        String priority = parts.length > 2 ? parts[2] : "MEDIUM";
                        Platform.runLater(() -> callback.onMoveTask(title, status, priority));
                    }

                    // ── EDIT TASK ──
                    else if (message.startsWith("EDIT|")) {
                        String[] parts = message.split("\\|", -1);
                        if (parts.length < 9) continue;
                        int taskId = Integer.parseInt(parts[1]);
                        String newTitle = parts[2];
                        String newStatus = parts[3];
                        String newPriority = parts[4];
                        String newDueDate = parts[5];
                        String newAssignee = parts[6];
                        String newCreator = parts[7];
                        String newCreatedAt = parts[8];
                        String actor = parts.length > 9 ? parts[9] : "";
                        Platform.runLater(() -> callback.onEditTask(taskId, newTitle, newStatus, newPriority, newDueDate, newAssignee, newCreator, newCreatedAt, actor));
                    }

                    // ── DELETE TASK ──
                    else if (message.startsWith("DELETE:")) {
                        String title = message.replace("DELETE:", "");
                        Platform.runLater(() -> callback.onDeleteTask(title));
                    }

                    // ── PRESENCE ONLINE ──
                    else if (message.startsWith("PRESENCE:ONLINE:")) {
                        String user = message.substring("PRESENCE:ONLINE:".length()).trim();
                        Platform.runLater(() -> callback.onPresenceOnline(user));
                    }

                    // ── PRESENCE OFFLINE ──
                    else if (message.startsWith("PRESENCE:OFFLINE:")) {
                        String user = message.substring("PRESENCE:OFFLINE:".length()).trim();
                        Platform.runLater(() -> callback.onPresenceOffline(user));
                    }

                    // ── PRESENCE LIST ──
                    else if (message.startsWith("PRESENCE:LIST:")) {
                        String usersList = message.substring("PRESENCE:LIST:".length()).trim();
                        Platform.runLater(() -> callback.onPresenceList(usersList));
                    }

                    // ── TYPING / EDITING ──
                    else if (message.startsWith("TYPING:")) {
                        String[] parts = message.split(":");
                        if (parts.length >= 4) {
                            String action = parts[1];
                            String user = parts[2];
                            int taskId = Integer.parseInt(parts[3]);
                            Platform.runLater(() -> callback.onTyping(action, user, taskId));
                        }
                    }

                    // ── COMMENT REAL-TIME SYNC ──
                    else if (message.startsWith("COMMENT:")) {
                        String[] parts = message.split(":");
                        if (parts.length >= 4) {
                            int taskId = Integer.parseInt(parts[2]);
                            Platform.runLater(() -> callback.onCommentSync(taskId));
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("[SOCKET] Listener disconnected: " + e.getMessage());
            }
        });
        listenThread.setDaemon(true);
        listenThread.start();
    }

    public void sendPresence(String username) {
        if (writer != null) {
            writer.println("PRESENCE:ONLINE:" + username);
        }
    }

    public void sendOfflinePresence(String username) {
        if (writer != null) {
            writer.println("PRESENCE:OFFLINE:" + username);
        }
    }

    public void sendNewTask(String title, String priority, String dueDate, String assignedTo, String createdBy) {
        if (writer != null) {
            writer.println("NEW TASK:" + title + ":" + priority + ":" + dueDate + ":" + assignedTo + ":" + createdBy);
        }
    }

    public void sendMoveTask(String title, String status, String priority) {
        if (writer != null) {
            writer.println("MOVE:" + title + ":" + status + ":" + priority);
        }
    }

    public void sendDeleteTask(String title) {
        if (writer != null) {
            writer.println("DELETE:" + title);
        }
    }
}
