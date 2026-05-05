package com.recetas_back.recetas_back.controller;

import com.recetas_back.recetas_back.dto.ValoracionRequest;
import com.recetas_back.recetas_back.model.Valoracion;
import com.recetas_back.recetas_back.service.ValoracionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/** API REST de Valoraciones. POST privado (requiere JWT). */
@RestController
@RequestMapping("/api/recetas/{id}/valoraciones")
public class ValoracionController {

    @Autowired
    private ValoracionService valoracionService;

    @GetMapping("/promedio")
    public ResponseEntity<Map<String,Object>> promedio(@PathVariable Long id) {
        double prom = valoracionService.obtenerPromedio(id);
        long total = valoracionService.obtenerTotal(id);
        return ResponseEntity.ok(Map.of(
            "promedio", Math.round(prom * 10.0) / 10.0,
            "total", total
        ));
    }

    @PostMapping
    public ResponseEntity<?> valorar(
            @PathVariable Long id,
            @RequestBody ValoracionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Valoracion v = valoracionService.guardarOActualizar(id, userDetails.getUsername(), request.getPuntuacion());
            return ResponseEntity.ok(v);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
