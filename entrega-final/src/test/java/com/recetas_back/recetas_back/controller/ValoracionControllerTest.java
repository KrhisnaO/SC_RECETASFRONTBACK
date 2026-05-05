package com.recetas_back.recetas_back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recetas_back.recetas_back.dto.ValoracionRequest;
import com.recetas_back.recetas_back.model.Valoracion;
import com.recetas_back.recetas_back.service.ValoracionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ValoracionController.class)
@Import(com.recetas_back.recetas_back.config.SecurityConfig.class)
@DisplayName("ValoracionController REST - pruebas unitarias")
class ValoracionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ValoracionService valoracionService;
    @MockBean com.recetas_back.recetas_back.security.JwtAuthenticationFilter jwtFilter;
    @MockBean com.recetas_back.recetas_back.security.CustomUserDetailsService userDetailsService;
    @MockBean org.springframework.security.authentication.AuthenticationManager authManager;

    @Test @DisplayName("GET /api/recetas/{id}/valoraciones/promedio retorna 200")
    void promedio_retorna200() throws Exception {
        when(valoracionService.obtenerPromedio(anyLong())).thenReturn(4.5);
        when(valoracionService.obtenerTotal(anyLong())).thenReturn(10L);
        mockMvc.perform(get("/api/recetas/1/valoraciones/promedio"))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("POST /api/recetas/{id}/valoraciones retorna 200 si autenticado")
    @WithMockUser(username = "carlos")
    void valorar_retorna200() throws Exception {
        Valoracion v = new Valoracion();
        v.setPuntuacion(5);
        when(valoracionService.guardarOActualizar(anyLong(), anyString(), anyInt())).thenReturn(v);

        ValoracionRequest req = new ValoracionRequest();
        req.setPuntuacion(5);
        mockMvc.perform(post("/api/recetas/1/valoraciones")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("POST /api/recetas/{id}/valoraciones retorna 404 si receta no existe")
    @WithMockUser(username = "carlos")
    void valorar_retorna404() throws Exception {
        doThrow(new IllegalArgumentException("No existe"))
                .when(valoracionService).guardarOActualizar(anyLong(), anyString(), anyInt());

        ValoracionRequest req = new ValoracionRequest();
        req.setPuntuacion(3);
        mockMvc.perform(post("/api/recetas/99/valoraciones")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }
}
