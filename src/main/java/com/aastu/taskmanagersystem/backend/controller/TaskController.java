package com.aastu.taskmanagersystem.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {

    @GetMapping("/hello")
    public String hello(){

        return "Spring Boot Works!";
    }
}