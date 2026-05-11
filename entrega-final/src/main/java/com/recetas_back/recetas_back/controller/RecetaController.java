package com.recetas_back.recetas_back.controller;

import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.service.RecetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * API REST de Recetas.
 * Rutas públicas:  GET /api/recetas/**
 * Rutas privadas:  POST /api/recetas       (publicar, requiere JWT)
 *                  DELETE /api/recetas/{id} (solo ROLE_ADMIN)
 */
@RestController
@RequestMapping("/api/recetas")
public class RecetaController {

    @Autowired
    private RecetaService recetaService;

    @GetMapping
    public ResponseEntity<List<Receta>> listarTodas() {
        return ResponseEntity.ok(recetaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        var receta = recetaService.obtenerPorId(id);
        if (receta.isEmpty()) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(receta.get());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Receta>> buscar(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tipoCocina,
            @RequestParam(required = false) String pais,
            @RequestParam(required = false) String dificultad) {
        return ResponseEntity.ok(recetaService.buscar(query, tipoCocina, pais, dificultad));
    }

    @GetMapping("/recientes")
    public ResponseEntity<List<Receta>> recientes(
            @RequestParam(required = false, defaultValue = "8") Integer limite) {
        return ResponseEntity.ok(recetaService.listarRecientes(limite));
    }

    @GetMapping("/populares")
    public ResponseEntity<List<Receta>> populares(
            @RequestParam(required = false, defaultValue = "8") Integer limite) {
        return ResponseEntity.ok(recetaService.listarPopulares(limite));
    }

    /**
     * Publica una nueva receta. Requiere JWT válido (usuario autenticado).
     * Body JSON esperado:
     * {
     *   "nombre": "...", "tipoCocina": "...", "pais": "...", "dificultad": "...",
     *   "tiempoPrepMinutos": 30, "descripcion": "...", "instrucciones": "...",
     *   "ingredientes": "...", "imagenUrl": "..."
     * }
     */
    @PostMapping
    public ResponseEntity<?> publicar(@RequestBody Map<String, Object> body,
                                       Principal principal) {
        try {
            
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Usuario no autenticado");
            }
            String username = principal.getName();

            Receta nueva = recetaService.crear(
                    (String) body.get("nombre"),
                    (String) body.get("tipoCocina"),
                    (String) body.get("pais"),
                    (String) body.get("dificultad"),
                    body.get("tiempoPrepMinutos") != null
                            ? Integer.parseInt(body.get("tiempoPrepMinutos").toString()) : 0,
                    (String) body.get("descripcion"),
                    (String) body.get("instrucciones"),
                    (String) body.get("ingredientes"),
                    (String) body.get("imagenUrl"),
                    username
            );
            return ResponseEntity.status(201).body(nueva);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Elimina una receta. Solo ROLE_ADMIN (controlado en SecurityConfig).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            recetaService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", "Receta no encontrada"));
        }
    }
}