package com.recetas_back.recetas_back.controller;

import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.service.RecetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * API REST de Recetas. Rutas públicas: GET.
 * Devuelve JSON consumido por el frontend independiente.
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
}
