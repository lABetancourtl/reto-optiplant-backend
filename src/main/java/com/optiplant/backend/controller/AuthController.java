package com.optiplant.backend.controller;

import org.springframework.web.bind.annotation.*;

import com.optiplant.backend.dto.LoginRequest;
import com.optiplant.backend.dto.LoginResponse;
import com.optiplant.backend.dto.RegisterRequest;
import com.optiplant.backend.entity.User;
import com.optiplant.backend.service.AuthService;

/**
 * Controlador REST para autenticación y registro de usuarios.
 * Expone endpoints para login y registro.
 * Permite crear usuarios y obtener tokens JWT para acceso seguro.
 * Seguridad: Los endpoints son públicos, pero el acceso posterior requiere token.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    /**
     * POST /auth/register
     * Registra un nuevo usuario (ADMIN o SUCURSAL).
     * Body: RegisterRequest (userName, password, role, nombre, etc.)
     * Retorna el usuario creado.
     */
    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request){
        return authService.register(request);
    }

    /**
     * POST /auth/login
     * Autentica usuario y retorna un token JWT.
     * Body: LoginRequest (userName, password)
     * Retorna LoginResponse (token, datos del usuario).
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request){
        return authService.login(request);
    }
}