package com.recetas_back.recetas_back.controller;

import com.recetas_back.recetas_back.dto.LoginRequest;
import com.recetas_back.recetas_back.dto.LoginResponse;
import com.recetas_back.recetas_back.dto.RegisterRequest;
import com.recetas_back.recetas_back.model.Usuario;
import com.recetas_back.recetas_back.repository.UsuarioRepository;
import com.recetas_back.recetas_back.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String jwt = authService.autenticar(request.getUsername(), request.getPassword());

            String role = usuarioRepository.findByUsername(request.getUsername())
                    .map(Usuario::getRole)
                    .orElse("ROLE_USER");

            return ResponseEntity.ok(new LoginResponse(jwt, request.getUsername(), role));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales invalidas"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El nombre de usuario y la contrasena son obligatorios."));
        }
        if (request.getPassword().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La contrasena debe tener al menos 6 caracteres."));
        }
        try {
            Usuario creado = authService.registrar(request);
            return ResponseEntity.status(201).body(Map.of(
                    "id",       creado.getId(),
                    "username", creado.getUsername(),
                    "role",     creado.getRole(),
                    "mensaje",  "Usuario registrado correctamente."
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(409).body(Map.of("error", ex.getMessage()));
        }
    }
}