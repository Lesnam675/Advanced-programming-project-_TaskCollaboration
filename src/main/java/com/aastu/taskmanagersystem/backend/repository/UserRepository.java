package com.aastu.taskmanagersystem.backend.repository;
import com.aastu.taskmanagersystem.backend.model.UserEntity;


import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository
        extends JpaRepository<UserEntity, Long> {

    UserEntity findByUsername(String username);
}