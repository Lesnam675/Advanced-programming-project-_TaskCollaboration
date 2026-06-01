package com.aastu.taskmanagersystem.backend.repository;
import com.aastu.taskmanagersystem.backend.model.TaskEntity;


import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository
        extends JpaRepository<TaskEntity, Integer> {

}