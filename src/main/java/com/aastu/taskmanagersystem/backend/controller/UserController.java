package com.aastu.taskmanagersystem.backend.controller;

import com.aastu.taskmanagersystem.backend.model.UserEntity;
import com.aastu.taskmanagersystem.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping("/register")
    public void registerUser(
            @RequestParam String username,
            @RequestParam String password
    ) {
        service.registerUser(username, password);
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password
    ) {
        UserEntity user = service.login(username, password);
        if (user != null) {
            return "SUCCESS";
        }
        return "FAILED";
    }

    @GetMapping
    public List<UserEntity> getAllUsers() {
        return service.getAllUsers();
    }
}