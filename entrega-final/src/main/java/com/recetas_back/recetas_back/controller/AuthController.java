package com.recetas_back.recetas_back.controller;

import com.recetas_back.recetas_back.dto.LoginRequest;
import com.recetas_back.recetas_back.dto.LoginResponse;
import com.recetas_back.recetas_back.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

/**
 * API REST de autenticación. Ruta pública: POST /api/auth/login
 * Devuelve JSON con el token JWT. El frontend lo almacena y lo envía
 * en el header Authorization: Bearer <token> en peticiones privadas.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String jwt = authService.autenticar(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(new LoginResponse(jwt, request.getUsername()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body("{\"error\":\"Credenciales inválidas\"}");
        }
    }
}
