package com.recetas_back.recetas_back.controller;

import com.recetas_back.recetas_back.dto.ComentarioRequest;
import com.recetas_back.recetas_back.exception.ContenidoNoPermitidoException;
import com.recetas_back.recetas_back.model.Comentario;
import com.recetas_back.recetas_back.service.ComentarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Comentario c = comentarioService.agregar(id, principal.getName(), request.getContenido());
            return ResponseEntity.status(HttpStatus.CREATED).body(c);
        } catch (ContenidoNoPermitidoException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error",  "Comentario rechazado por moderación",
                    "motivo", e.getMessage()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
