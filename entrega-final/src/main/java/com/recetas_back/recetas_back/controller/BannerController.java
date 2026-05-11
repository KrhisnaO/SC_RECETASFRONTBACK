package com.recetas_back.recetas_back.controller;

import com.recetas_back.recetas_back.model.Banner;
import com.recetas_back.recetas_back.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banners")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping
    public ResponseEntity<List<Banner>> listarActivos() {
        return ResponseEntity.ok(bannerService.listarActivos());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Banner>> listarTodos() {
        return ResponseEntity.ok(bannerService.listarTodos());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            Banner b = bannerService.crear(
                    (String) body.get("titulo"),
                    (String) body.get("empresa"),
                    (String) body.get("imagenUrl"),
                    (String) body.get("enlaceUrl"),
                    body.get("activo") != null ? Boolean.valueOf(body.get("activo").toString()) : null,
                    body.get("orden") != null ? Integer.valueOf(body.get("orden").toString()) : null
            );
            return ResponseEntity.status(201).body(b);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Banner b = bannerService.actualizar(
                    id,
                    (String) body.get("titulo"),
                    (String) body.get("empresa"),
                    (String) body.get("imagenUrl"),
                    (String) body.get("enlaceUrl"),
                    body.get("activo") != null ? Boolean.valueOf(body.get("activo").toString()) : null,
                    body.get("orden") != null ? Integer.valueOf(body.get("orden").toString()) : null
            );
            return ResponseEntity.ok(b);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            bannerService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}
