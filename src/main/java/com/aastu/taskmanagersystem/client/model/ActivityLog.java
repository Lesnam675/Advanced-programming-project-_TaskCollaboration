package com.aastu.taskmanagersystem.client.model;
import com.aastu.taskmanagersystem.client.ui.Main;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ActivityLog {

    public enum Action {
        CREATED("➕ Created"),
        EDITED("✏️ Edited"),
        MOVED("🔀 Moved"),
        DELETED("🗑️ Deleted"),
        ASSIGNED("👤 Assigned");

        private final String label;
        Action(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    
    private final String username;
    private final Action action;
    private final String taskName;
    private final LocalDateTime timestamp;

    private ActivityLog(String username, Action action, String taskName) {
        this.username  = username  == null ? "System"   : username;
        this.action    = action    == null ? Action.EDITED : action;
        this.taskName  = taskName  == null ? ""          : taskName;
        this.timestamp = LocalDateTime.now();
    }

    
    private static final int MAX_ENTRIES = 200;
    private static final List<ActivityLog> LOG =
            Collections.synchronizedList(new ArrayList<>());

    public static void addEntry(String username, Action action, String taskName) {
        ActivityLog entry = new ActivityLog(username, action, taskName);
        LOG.add(0, entry);                       
        if (LOG.size() > MAX_ENTRIES) {
            LOG.remove(LOG.size() - 1);
        }
    }


    public static List<ActivityLog> getRecent(int max) {
        synchronized (LOG) {
            int to = Math.min(max, LOG.size());
            return new ArrayList<>(LOG.subList(0, to));
        }
    }


    public String getUsername()  { return username; }
    public Action getAction()    { return action; }
    public String getTaskName()  { return taskName; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    public String getFormattedDate() {
        return timestamp.format(DateTimeFormatter.ofPattern("MMM d, HH:mm"));
    }
}
