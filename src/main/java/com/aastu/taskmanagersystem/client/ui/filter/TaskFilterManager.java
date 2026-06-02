package com.aastu.taskmanagersystem.client.ui.filter;

import com.aastu.taskmanagersystem.client.model.TaskItem;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class TaskFilterManager {

    private TaskFilterManager() {}

    public static boolean isOverdue(TaskItem item) {
        try {
            if (item.getDueDate() == null || item.getDueDate().isBlank()) return false;
            if ("DONE".equalsIgnoreCase(item.getStatus())) return false;
            return java.time.LocalDate.parse(item.getDueDate()).isBefore(java.time.LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    private static java.time.LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try { return java.time.LocalDate.parse(value); } catch (Exception e) { return null; }
    }

    private static int compareDueDate(TaskItem a, TaskItem b) {
        java.time.LocalDate da = parseDate(a.getDueDate());
        java.time.LocalDate db = parseDate(b.getDueDate());
        if (da == null && db == null) return a.getTitle().compareToIgnoreCase(b.getTitle());
        if (da == null) return 1;
        if (db == null) return -1;
        int cmp = da.compareTo(db);
        return cmp != 0 ? cmp : a.getTitle().compareToIgnoreCase(b.getTitle());
    }

    private static Comparator<TaskItem> taskSortComparator(String sort) {
        return (a, b) -> {
            if ("Newest first".equals(sort)) return compareDueDate(b, a);
            if ("Oldest first".equals(sort)) return compareDueDate(a, b);
            boolean overdueA = isOverdue(a);
            boolean overdueB = isOverdue(b);
            if (overdueA != overdueB) return overdueA ? -1 : 1;
            return compareDueDate(a, b);
        };
    }

    public static List<TaskItem> getFilteredTasks(
            List<TaskItem> taskCache,
            String search,
            String priority,
            String assignee,
            String creator,
            String status,
            String sort,
            boolean overdueOnly,
            String currentUser
    ) {
        final String searchClean = search == null ? "" : search.trim().toLowerCase();

        return taskCache.stream()
                .filter(t -> searchClean.isEmpty() || t.getTitle().toLowerCase().contains(searchClean))
                .filter(t -> "All".equals(priority)
                        || (t.getPriority() != null && priority.equalsIgnoreCase(t.getPriority())))
                .filter(t -> "All".equals(assignee)
                        || (t.getAssignedTo() != null && assignee.equalsIgnoreCase(t.getAssignedTo())))
                .filter(t -> "All".equals(creator)
                        || (t.getCreatedBy() != null && creator.equalsIgnoreCase(t.getCreatedBy())))
                .filter(t -> "All".equals(status)
                        || (t.getStatus() != null && status.equalsIgnoreCase(t.getStatus())))
                .filter(t -> !overdueOnly || isOverdue(t))
                .sorted(taskSortComparator(sort))
                .collect(Collectors.toList());
    }
}
