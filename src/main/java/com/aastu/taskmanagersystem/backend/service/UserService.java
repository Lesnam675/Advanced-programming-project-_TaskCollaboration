package com.aastu.taskmanagersystem.backend.service;

import com.aastu.taskmanagersystem.backend.repository.UserRepository;
import com.aastu.taskmanagersystem.backend.model.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public void registerUser(String username, String password) {
        UserEntity existing = repository.findByUsername(username);
        if (existing != null) {
            return;
        }
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(password);
        repository.save(user);
    }

    public UserEntity login(String username, String password) {
        UserEntity user = repository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public List<UserEntity> getAllUsers() {
        return repository.findAll();
    }
}