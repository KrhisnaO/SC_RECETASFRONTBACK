package com.recetas_back.recetas_back.service;

import com.recetas_back.recetas_back.dto.RegisterRequest;
import com.recetas_back.recetas_back.model.Usuario;
import com.recetas_back.recetas_back.repository.UsuarioRepository;
import com.recetas_back.recetas_back.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String autenticar(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return tokenProvider.generateToken(authentication);
    }

    public String obtenerRole(String username) {
        return usuarioRepository.findByUsername(username)
                .map(Usuario::getRole)
                .orElse("ROLE_USER");
    }

    public Usuario registrar(RegisterRequest request) {
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya esta en uso.");
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (usuarioRepository.findByCorreo(request.getEmail()).isPresent()) {
                throw new IllegalArgumentException("El correo electronico ya esta registrado.");
            }
        }
        Usuario nuevo = new Usuario();
        nuevo.setUsername(request.getUsername());
        nuevo.setNombre(request.getNombre() != null ? request.getNombre() : request.getUsername());
        nuevo.setCorreo(request.getEmail());
        nuevo.setPassword(passwordEncoder.encode(request.getPassword()));
        nuevo.setRole("ROLE_USER");
        return usuarioRepository.save(nuevo);
    }
}