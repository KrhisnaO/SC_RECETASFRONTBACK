package com.recetas_back.recetas_back.controller;

import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.service.RecetaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MediaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.recetas_back.recetas_back.config.SecurityConfig.class)
@DisplayName("MediaController REST - pruebas unitarias")
class MediaControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean RecetaService recetaService;
    @MockBean com.recetas_back.recetas_back.security.JwtAuthenticationFilter jwtFilter;
    @MockBean com.recetas_back.recetas_back.security.CustomUserDetailsService userDetailsService;
    @MockBean org.springframework.security.authentication.AuthenticationManager authManager;

    private Receta recetaConMedia() {
        Receta r = new Receta();
        r.setId(1L);
        r.setNombre("Tacos");
        r.setElementosMedia(new ArrayList<>());
        return r;
    }

    @Test
    @DisplayName("POST /api/recetas/{id}/media → 400 si archivo vacío")
    @WithMockUser
    void upload_archivoVacio_retorna400() throws Exception {
        MockMultipartFile vacio = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg", new byte[0]);
        mockMvc.perform(multipart("/api/recetas/1/media").file(vacio).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/recetas/{id}/media → 400 si tipo MIME no permitido")
    @WithMockUser
    void upload_tipoNoPermitido_retorna400() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "datos".getBytes());
        mockMvc.perform(multipart("/api/recetas/1/media").file(pdf).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/recetas/{id}/media → 400 si extensión no permitida")
    @WithMockUser
    void upload_extensionNoPermitida_retorna400() throws Exception {
        MockMultipartFile exe = new MockMultipartFile(
                "file", "virus.exe", "image/jpeg", "datos".getBytes());
        mockMvc.perform(multipart("/api/recetas/1/media").file(exe).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/recetas/{id}/media → 404 si receta no existe")
    @WithMockUser
    void upload_recetaNoExiste_retorna404() throws Exception {
        when(recetaService.obtenerPorId(99L)).thenReturn(Optional.empty());
        MockMultipartFile img = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg", "imagen-valida".getBytes());
        mockMvc.perform(multipart("/api/recetas/99/media").file(img).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/recetas/{id}/media → 201 con imagen válida")
    @WithMockUser
    void upload_imagenValida_retorna201() throws Exception {
        Receta receta = recetaConMedia();
        when(recetaService.obtenerPorId(1L)).thenReturn(Optional.of(receta));
        when(recetaService.guardar(any(Receta.class))).thenReturn(receta);
        MockMultipartFile img = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg", "contenido-imagen".getBytes());
        mockMvc.perform(multipart("/api/recetas/1/media").file(img).with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/recetas/{id}/media → 400 si archivo supera 50MB")
    @WithMockUser
    void upload_archivoMuyGrande_retorna400() throws Exception {
        byte[] grande = new byte[51 * 1024 * 1024];
        MockMultipartFile img = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg", grande);
        mockMvc.perform(multipart("/api/recetas/1/media").file(img).with(csrf()))
                .andExpect(status().isBadRequest());
    }
}