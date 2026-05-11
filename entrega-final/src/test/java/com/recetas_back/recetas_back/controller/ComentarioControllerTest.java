package com.recetas_back.recetas_back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recetas_back.recetas_back.dto.ComentarioRequest;
import com.recetas_back.recetas_back.model.Comentario;
import com.recetas_back.recetas_back.service.ComentarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ComentarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.recetas_back.recetas_back.config.SecurityConfig.class)
@DisplayName("ComentarioController REST - pruebas unitarias")
class ComentarioControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ComentarioService comentarioService;
    @MockBean com.recetas_back.recetas_back.security.JwtAuthenticationFilter jwtFilter;
    @MockBean com.recetas_back.recetas_back.security.CustomUserDetailsService userDetailsService;
    @MockBean org.springframework.security.authentication.AuthenticationManager authManager;

    @Test @DisplayName("GET /api/recetas/{id}/comentarios retorna 200")
    void listar_retorna200() throws Exception {
        Comentario c = new Comentario();
        c.setContenido("Rico!");
        when(comentarioService.listarPorReceta(1L)).thenReturn(List.of(c));
        mockMvc.perform(get("/api/recetas/1/comentarios"))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/recetas/{id}/comentarios retorna 200 vacío")
    void listar_retornaVacio() throws Exception {
        when(comentarioService.listarPorReceta(anyLong())).thenReturn(List.of());
        mockMvc.perform(get("/api/recetas/1/comentarios"))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("POST /api/recetas/{id}/comentarios retorna 201 si autenticado")
    @WithMockUser(username = "maria")
    void agregar_retorna201() throws Exception {
        Comentario c = new Comentario();
        c.setContenido("Muy buena!");
        when(comentarioService.agregar(anyLong(), anyString(), anyString())).thenReturn(c);

        ComentarioRequest req = new ComentarioRequest();
        req.setContenido("Muy buena!");
        mockMvc.perform(post("/api/recetas/1/comentarios")
                        .with(csrf())
                        .principal(() -> "maria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test @DisplayName("POST /api/recetas/{id}/comentarios retorna 404 si receta no existe")
    @WithMockUser(username = "maria")
    void agregar_retorna404() throws Exception {
        doThrow(new IllegalArgumentException("Receta no encontrada"))
                .when(comentarioService).agregar(anyLong(), anyString(), anyString());

        ComentarioRequest req = new ComentarioRequest();
        req.setContenido("Texto");
        mockMvc.perform(post("/api/recetas/99/comentarios")
                        .with(csrf())
                        .principal(() -> "maria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }
}
