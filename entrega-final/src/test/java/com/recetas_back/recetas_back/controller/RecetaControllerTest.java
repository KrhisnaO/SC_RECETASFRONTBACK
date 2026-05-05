package com.recetas_back.recetas_back.controller;

import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.service.RecetaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecetaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.recetas_back.recetas_back.config.SecurityConfig.class)
@DisplayName("RecetaController REST - pruebas unitarias")
class RecetaControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean RecetaService recetaService;
    @MockBean com.recetas_back.recetas_back.security.JwtAuthenticationFilter jwtFilter;
    @MockBean com.recetas_back.recetas_back.security.CustomUserDetailsService userDetailsService;
    @MockBean org.springframework.security.authentication.AuthenticationManager authManager;

    private Receta receta;

    @BeforeEach
    void setUp() {
        receta = new Receta();
        receta.setId(1L);
        receta.setNombre("Paella");
        receta.setTipoCocina("Espanola");
        receta.setDificultad("Media");
        receta.setTiempoPrepMinutos(60);
    }

    @Test @DisplayName("GET /api/recetas retorna 200")
    void listarTodas_retorna200() throws Exception {
        when(recetaService.listarTodas()).thenReturn(List.of(receta));
        mockMvc.perform(get("/api/recetas"))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/recetas retorna 200 con lista vacía")
    void listarTodas_retornaVacio() throws Exception {
        when(recetaService.listarTodas()).thenReturn(List.of());
        mockMvc.perform(get("/api/recetas"))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/recetas/{id} retorna 200 si existe")
    void obtenerPorId_retorna200() throws Exception {
        when(recetaService.obtenerPorId(1L)).thenReturn(Optional.of(receta));
        mockMvc.perform(get("/api/recetas/1"))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/recetas/{id} retorna 404 si no existe")
    void obtenerPorId_retorna404() throws Exception {
        when(recetaService.obtenerPorId(anyLong())).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/recetas/99"))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("GET /api/recetas/buscar retorna 200")
    void buscar_retorna200() throws Exception {
        when(recetaService.buscar(any(), any(), any(), any())).thenReturn(List.of(receta));
        mockMvc.perform(get("/api/recetas/buscar").param("query", "paella"))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/recetas/buscar retorna 200 sin resultados")
    void buscar_retornaVacio() throws Exception {
        when(recetaService.buscar(any(), any(), any(), any())).thenReturn(List.of());
        mockMvc.perform(get("/api/recetas/buscar"))
                .andExpect(status().isOk());
    }
}
