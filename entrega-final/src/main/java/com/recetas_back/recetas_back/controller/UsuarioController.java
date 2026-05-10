package com.recetas_back.recetas_back.controller;

import com.recetas_back.recetas_back.model.Usuario;
import com.recetas_back.recetas_back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * API REST para gestión del perfil del usuario autenticado.
 *
 * GET  /api/usuario/perfil           → devuelve datos del perfil
 * PUT  /api/usuario/perfil           → actualiza nombre y correo
 * PUT  /api/usuario/cambiar-password → cambia la contraseña
 */
@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Devuelve los datos del usuario autenticado (sin contraseña).
     */
    @GetMapping("/perfil")
    public ResponseEntity<?> getPerfil(Authentication auth) {
        String username = auth.getName();
        Optional<Usuario> opt = usuarioRepository.findByUsername(username);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado."));
        }
        Usuario u = opt.get();
        return ResponseEntity.ok(Map.of(
                "id",       u.getId(),
                "username", u.getUsername(),
                "nombre",   u.getNombre()  != null ? u.getNombre()  : "",
                "correo",   u.getCorreo()  != null ? u.getCorreo()  : "",
                "role",     u.getRole()    != null ? u.getRole()    : ""
        ));
    }

    /**
     * Actualiza nombre y/o correo del usuario autenticado.
     * Body JSON: { "nombre": "...", "correo": "..." }
     */
    @PutMapping("/perfil")
    public ResponseEntity<?> actualizarPerfil(@RequestBody Map<String, String> body,
                                               Authentication auth) {
        String username = auth.getName();
        Optional<Usuario> opt = usuarioRepository.findByUsername(username);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado."));
        }
        Usuario u = opt.get();

        String nuevoNombre = body.get("nombre");
        String nuevoCorreo = body.get("correo");

        if (nuevoNombre != null && !nuevoNombre.isBlank()) {
            u.setNombre(nuevoNombre.trim());
        }

        if (nuevoCorreo != null && !nuevoCorreo.isBlank()) {
            // Verificar que el correo no esté ya en uso por otro usuario
            Optional<Usuario> existe = usuarioRepository.findByCorreo(nuevoCorreo.trim());
            if (existe.isPresent() && !existe.get().getId().equals(u.getId())) {
                return ResponseEntity.status(409)
                        .body(Map.of("error", "El correo ya está registrado por otro usuario."));
            }
            u.setCorreo(nuevoCorreo.trim());
        }

        usuarioRepository.save(u);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Perfil actualizado correctamente.",
                "nombre",  u.getNombre()  != null ? u.getNombre()  : "",
                "correo",  u.getCorreo()  != null ? u.getCorreo()  : ""
        ));
    }

    /**
     * Cambia la contraseña del usuario autenticado.
     * Body JSON: { "passwordActual": "...", "passwordNueva": "..." }
     */
    @PutMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(@RequestBody Map<String, String> body,
                                              Authentication auth) {
        String username = auth.getName();
        Optional<Usuario> opt = usuarioRepository.findByUsername(username);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado."));
        }
        Usuario u = opt.get();

        String passwordActual = body.get("passwordActual");
        String passwordNueva  = body.get("passwordNueva");

        if (passwordActual == null || passwordActual.isBlank()
                || passwordNueva == null || passwordNueva.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ambas contraseñas son obligatorias."));
        }

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(passwordActual, u.getPassword())) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "La contraseña actual es incorrecta."));
        }

        if (passwordNueva.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La nueva contraseña debe tener al menos 6 caracteres."));
        }

        u.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(u);

        return ResponseEntity.ok(Map.of("mensaje", "Contraseña cambiada correctamente."));
    }
}