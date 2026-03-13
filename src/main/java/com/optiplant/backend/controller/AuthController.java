package com.optiplant.backend.controller;

import org.springframework.web.bind.annotation.*;

import com.optiplant.backend.dto.LoginRequest;
import com.optiplant.backend.dto.LoginResponse;
import com.optiplant.backend.dto.RegisterRequest;
import com.optiplant.backend.entity.User;
import com.optiplant.backend.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request){
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request){
        return authService.login(request);
    }
}