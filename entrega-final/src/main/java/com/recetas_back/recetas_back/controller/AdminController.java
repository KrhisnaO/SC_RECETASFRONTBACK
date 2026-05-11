package com.recetas_back.recetas_back.controller;

import com.recetas_back.recetas_back.model.Comentario;
import com.recetas_back.recetas_back.model.Comentario.Estado;
import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.model.Usuario;
import com.recetas_back.recetas_back.service.AdminService;
import com.recetas_back.recetas_back.service.ComentarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ComentarioService comentarioService;

    // Usuarios

    @GetMapping("/usuarios")
    public ResponseEntity<List<Map<String, Object>>> listarUsuarios() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Usuario u : adminService.listarUsuarios()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id",       u.getId());
            map.put("username", u.getUsername());
            map.put("nombre",   u.getNombre() != null ? u.getNombre() : "");
            map.put("correo",   u.getCorreo() != null ? u.getCorreo() : "");
            map.put("role",     u.getRole() != null ? u.getRole() : "ROLE_USER");
            resultado.add(map);
        }
        return ResponseEntity.ok(resultado);
    }

    @PutMapping("/usuarios/{id}/rol")
    public ResponseEntity<?> cambiarRol(@PathVariable Long id,
                                         @RequestBody Map<String, String> body) {
        String nuevoRol = body.get("role");
        if (nuevoRol == null || nuevoRol.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El campo 'role' es obligatorio."));
        }
        if (!nuevoRol.equals("ROLE_USER") && !nuevoRol.equals("ROLE_ADMIN")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rol invalido. Use ROLE_USER o ROLE_ADMIN."));
        }
        try {
            Usuario actualizado = adminService.cambiarRol(id, nuevoRol);
            return ResponseEntity.ok(Map.of(
                "id",       actualizado.getId(),
                "username", actualizado.getUsername(),
                "role",     actualizado.getRole()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        try {
            adminService.eliminarUsuario(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Comentarios

    @GetMapping("/comentarios")
    public ResponseEntity<List<Map<String, Object>>> listarComentarios(
            @RequestParam(required = false) String estado) {
        List<Comentario> base = obtenerComentariosPorEstado(estado);
        if (base == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(base.stream().map(this::comentarioToMap).toList());
    }

    private List<Comentario> obtenerComentariosPorEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return adminService.listarTodosComentarios();
        }
        try {
            return comentarioService.listarPorEstado(Estado.valueOf(estado.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private Map<String, Object> comentarioToMap(Comentario c) {
        Map<String, Object> map = new HashMap<>();
        map.put("id",            c.getId());
        map.put("contenido",     c.getContenido() != null ? c.getContenido() : "");
        map.put("usuario",       c.getUsuario() != null ? c.getUsuario().getUsername() : "Eliminado");
        map.put("recetaId",      c.getReceta() != null ? c.getReceta().getId() : 0L);
        map.put("recetaNombre",  c.getReceta() != null ? c.getReceta().getNombre() : "");
        map.put("createdAt",     c.getCreatedAt() != null ? c.getCreatedAt().toString() : "");
        map.put("estado",        c.getEstado() != null ? c.getEstado().name() : Estado.PENDIENTE.name());
        map.put("motivoRechazo", c.getMotivoRechazo() != null ? c.getMotivoRechazo() : "");
        return map;
    }

    @PutMapping("/comentarios/{id}/aprobar")
    public ResponseEntity<?> aprobarComentario(@PathVariable Long id) {
        try {
            Comentario c = comentarioService.aprobar(id);
            return ResponseEntity.ok(Map.of(
                    "id", c.getId(),
                    "estado", c.getEstado().name()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/comentarios/{id}/rechazar")
    public ResponseEntity<?> rechazarComentario(@PathVariable Long id,
                                                 @RequestBody(required = false) Map<String, String> body) {
        try {
            String motivo = body != null ? body.get("motivo") : null;
            Comentario c = comentarioService.rechazar(id, motivo);
            return ResponseEntity.ok(Map.of(
                    "id", c.getId(),
                    "estado", c.getEstado().name(),
                    "motivoRechazo", c.getMotivoRechazo() != null ? c.getMotivoRechazo() : ""
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/comentarios/{id}")
    public ResponseEntity<?> eliminarComentario(@PathVariable Long id) {
        try {
            adminService.eliminarComentario(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Recetas

    @GetMapping("/recetas")
    public ResponseEntity<List<Map<String, Object>>> listarRecetas() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Receta r : adminService.listarTodasRecetas()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id",       r.getId());
            map.put("nombre",   r.getNombre());
            map.put("pais",     r.getPais() != null ? r.getPais() : "");
            map.put("dificultad", r.getDificultad() != null ? r.getDificultad() : "");
            resultado.add(map);
        }
        return ResponseEntity.ok(resultado);
    }

    @DeleteMapping("/recetas/{id}")
    public ResponseEntity<?> eliminarReceta(@PathVariable Long id) {
        try {
            adminService.eliminarReceta(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}
