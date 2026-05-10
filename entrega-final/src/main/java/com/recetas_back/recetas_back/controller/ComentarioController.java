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
import org.springframework.http.HttpStatus;

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
        java.security.Principal principal) {

    try {
         if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = principal.getName();

        Comentario c = comentarioService.agregar(id, username, request.getContenido());

        return ResponseEntity.status(201).body(c);

    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(404).build();
    }
}
}

