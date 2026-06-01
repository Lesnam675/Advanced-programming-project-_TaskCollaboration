package com.aastu.taskmanagersystem.backend.repository;
import com.aastu.taskmanagersystem.backend.model.CommentEntity;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {
    List<CommentEntity> findByTaskId(int taskId);
}
