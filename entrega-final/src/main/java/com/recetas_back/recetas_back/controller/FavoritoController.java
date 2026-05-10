package com.recetas_back.recetas_back.controller;

import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.service.FavoritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritoController {

    @Autowired
    private FavoritoService favoritoService;

    @GetMapping
    public ResponseEntity<?> listar(Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no autenticado"));
        }

        String username = principal.getName();
        List<Receta> favoritos = favoritoService.listar(username);

        return ResponseEntity.ok(favoritos);
    }

    @PostMapping("/{recetaId}")
    public ResponseEntity<?> agregar(@PathVariable Long recetaId,
                                     Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no autenticado"));
        }

        try {
            String username = principal.getName();

            favoritoService.agregar(username, recetaId);

            return ResponseEntity.ok(
                    Map.of("mensaje", "Receta agregada a favoritos")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{recetaId}")
    public ResponseEntity<?> eliminar(@PathVariable Long recetaId,
                                      Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no autenticado"));
        }

        try {
            String username = principal.getName();

            favoritoService.eliminar(username, recetaId);

            return ResponseEntity.ok(
                    Map.of("mensaje", "Receta eliminada de favoritos")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{recetaId}/es")
    public ResponseEntity<?> esFavorito(@PathVariable Long recetaId,
                                        Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no autenticado"));
        }

        String username = principal.getName();

        boolean esFavorito = favoritoService.esFavorito(username, recetaId);

        return ResponseEntity.ok(
                Map.of("esFavorito", esFavorito)
        );
    }
}