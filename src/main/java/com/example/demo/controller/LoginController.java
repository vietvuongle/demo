package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class LoginController {
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    /**
     * Xử lý đăng xuất người dùng
     * - Xóa session
     * - Xóa authentication
     * - Chuyển hướng đến trang đăng nhập
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, 
                         HttpServletResponse response, 
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        
        if (authentication != null) {
            // Xóa authentication từ Spring Security
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        
        // Invalidate session
        request.getSession().invalidate();
        
        // Chuyển hướng đến trang đăng nhập
        return "redirect:/login";
    }
}