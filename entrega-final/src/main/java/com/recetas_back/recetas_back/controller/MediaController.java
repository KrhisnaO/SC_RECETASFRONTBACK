package com.recetas_back.recetas_back.controller;

import com.recetas_back.recetas_back.model.Multimedia;
import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.service.RecetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * API REST para subida de multimedia (imágenes y videos).
 * SAST Fix: lista blanca de MIME types y extensiones (CWE-434 / OWASP A04:2021).
 */
@RestController
@RequestMapping("/api/recetas/{id}/media")
public class MediaController {

    private static final Logger LOGGER = Logger.getLogger(MediaController.class.getName());

    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "video/mp4", "video/webm", "video/ogg");

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp",
            ".mp4", ".webm", ".ogv");

    private static final long MAX_FILE_SIZE_BYTES = 50L * 1024 * 1024;
    private final String UPLOAD_DIR = "./uploads/";

    @Autowired
    private RecetaService recetaService;

    @PostMapping
    public ResponseEntity<?> uploadMedia(@PathVariable Long id,
                                          @RequestParam("file") MultipartFile file) {
        if (file.isEmpty())
            return ResponseEntity.badRequest().body("Archivo vacío");
        if (file.getSize() > MAX_FILE_SIZE_BYTES)
            return ResponseEntity.badRequest().body("Archivo demasiado grande");

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase()))
            return ResponseEntity.badRequest().body("Tipo de archivo no permitido");

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank())
            return ResponseEntity.badRequest().body("Nombre de archivo inválido");

        String lowerName = originalName.toLowerCase();
        boolean extensionOk = ALLOWED_EXTENSIONS.stream().anyMatch(lowerName::endsWith);
        if (!extensionOk)
            return ResponseEntity.badRequest().body("Extensión no permitida");

        Receta receta = recetaService.obtenerPorId(id).orElse(null);
        if (receta == null)
            return ResponseEntity.notFound().build();

        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            String extension = lowerName.substring(lowerName.lastIndexOf('.'));
            String safeFileName = UUID.randomUUID() + extension;
            Path path = Paths.get(UPLOAD_DIR + safeFileName);
            Files.write(path, file.getBytes());

            Multimedia media = new Multimedia();
            media.setFileUrl("/uploads/" + safeFileName);
            media.setFileType(contentType.startsWith("video") ? "VIDEO" : "FOTO");
            media.setReceta(receta);

            receta.getElementosMedia().add(media);
            recetaService.guardar(receta);

            return ResponseEntity.status(201).body(media);
        } catch (IOException e) {
            LOGGER.severe("Error al guardar multimedia: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error al guardar el archivo");
        }
    }
}
