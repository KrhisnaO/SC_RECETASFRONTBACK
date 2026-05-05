package com.recetas_back.recetas_back.controller;

import com.recetas_back.recetas_back.dto.ComentarioRequest;
import com.recetas_back.recetas_back.model.Comentario;
import com.recetas_back.recetas_back.service.ComentarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** API REST de Comentarios. GET público, POST privado (requiere JWT). */
@RestController
@RequestMapping("/api/recetas/{id}/comentarios")
public class ComentarioController {

    @Autowired
    private ComentarioService comentarioService;

    @GetMapping
    public ResponseEntity<List<Comentario>> listar(@PathVariable Long id) {
        return ResponseEntity.ok(comentarioService.listarPorReceta(id));
    }

    @PostMapping
    public ResponseEntity<?> agregar(
            @PathVariable Long id,
            @RequestBody ComentarioRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Comentario c = comentarioService.agregar(id, userDetails.getUsername(), request.getContenido());
            return ResponseEntity.status(201).body(c);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
