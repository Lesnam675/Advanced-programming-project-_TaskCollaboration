package com.aastu.taskmanagersystem.backend.repository;
import com.aastu.taskmanagersystem.backend.model.TaskHistoryEntity;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistoryEntity, Integer> {
    List<TaskHistoryEntity> findByTaskIdOrderByIdAsc(int taskId);
}
