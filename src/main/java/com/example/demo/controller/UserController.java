package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequestMapping("/user")
public class UserController {
    @GetMapping("/dashboard")
    public String userDashboard() {
        return "user/dashboard";
    }
    
}
