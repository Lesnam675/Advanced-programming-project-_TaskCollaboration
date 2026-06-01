package com.aastu.taskmanagersystem.client.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory notification store – Phase 4.
 * Notifications are generated whenever an event involves the current user's tasks.
 */
public class NotificationStore {

    // ── fields ────────────────────────────────────────────────
    private final String message;
    private final String taskName;
    private final LocalDateTime timestamp;
    private boolean read;

    private NotificationStore(String message, String taskName) {
        this.message   = message  == null ? "" : message;
        this.taskName  = taskName == null ? "" : taskName;
        this.timestamp = LocalDateTime.now();
        this.read      = false;
    }

    // ── static store ──────────────────────────────────────────
    private static final int MAX = 100;
    private static final List<NotificationStore> STORE =
            Collections.synchronizedList(new ArrayList<>());

    public static void add(String message, String taskName) {
        NotificationStore n = new NotificationStore(message, taskName);
        STORE.add(0, n);                         // newest first
        if (STORE.size() > MAX) {
            STORE.remove(STORE.size() - 1);
        }
    }

    public static int unreadCount() {
        synchronized (STORE) {
            return (int) STORE.stream().filter(n -> !n.read).count();
        }
    }

    public static List<NotificationStore> getAll() {
        synchronized (STORE) {
            return new ArrayList<>(STORE);
        }
    }

    public static void markAllRead() {
        synchronized (STORE) {
            STORE.forEach(n -> n.read = true);
        }
    }

    // ── accessors ─────────────────────────────────────────────
    public String getMessage()  { return message; }
    public String getTaskName() { return taskName; }
    public boolean isRead()     { return read; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("MMM d, HH:mm"));
    }
}
