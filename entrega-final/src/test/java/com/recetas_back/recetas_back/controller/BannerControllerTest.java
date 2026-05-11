package com.recetas_back.recetas_back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recetas_back.recetas_back.model.Banner;
import com.recetas_back.recetas_back.service.BannerService;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BannerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.recetas_back.recetas_back.config.SecurityConfig.class)
@DisplayName("BannerController REST - pruebas unitarias")
class BannerControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean BannerService bannerService;
    @MockBean com.recetas_back.recetas_back.security.JwtAuthenticationFilter jwtFilter;
    @MockBean com.recetas_back.recetas_back.security.CustomUserDetailsService userDetailsService;
    @MockBean org.springframework.security.authentication.AuthenticationManager authManager;

    private Banner banner;

    @BeforeEach
    void setUp() {
        banner = new Banner();
        banner.setId(1L);
        banner.setTitulo("Promo");
        banner.setImagenUrl("https://img.com/x.jpg");
        banner.setActivo(true);
    }

    @Test @DisplayName("GET /api/banners → 200 con activos")
    void listarActivos_retorna200() throws Exception {
        when(bannerService.listarActivos()).thenReturn(List.of(banner));
        mockMvc.perform(get("/api/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Promo"));
    }

    @Test @DisplayName("GET /api/banners/admin → 200 con todos (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void listarTodos_admin_retorna200() throws Exception {
        when(bannerService.listarTodos()).thenReturn(List.of(banner));
        mockMvc.perform(get("/api/banners/admin"))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("POST /api/banners → 201 si datos válidos (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void crear_datosValidos_retorna201() throws Exception {
        when(bannerService.crear(any(), any(), any(), any(), any(), any())).thenReturn(banner);
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Promo",
                "imagenUrl", "https://img.com/x.jpg",
                "activo", true,
                "orden", 1
        ));
        mockMvc.perform(post("/api/banners").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test @DisplayName("POST /api/banners → 400 si título vacío")
    @WithMockUser(roles = "ADMIN")
    void crear_tituloVacio_retorna400() throws Exception {
        when(bannerService.crear(any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("El título es obligatorio."));
        mockMvc.perform(post("/api/banners").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\",\"imagenUrl\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test @DisplayName("PUT /api/banners/{id} → 200 si existe")
    @WithMockUser(roles = "ADMIN")
    void actualizar_existe_retorna200() throws Exception {
        when(bannerService.actualizar(eq(1L), any(), any(), any(), any(), any(), any()))
                .thenReturn(banner);
        mockMvc.perform(put("/api/banners/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Updated\"}"))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("PUT /api/banners/{id} → 404 si no existe")
    @WithMockUser(roles = "ADMIN")
    void actualizar_noExiste_retorna404() throws Exception {
        when(bannerService.actualizar(eq(99L), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Banner no encontrado"));
        mockMvc.perform(put("/api/banners/99").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"x\"}"))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("DELETE /api/banners/{id} → 204 si existe")
    @WithMockUser(roles = "ADMIN")
    void eliminar_existe_retorna204() throws Exception {
        doNothing().when(bannerService).eliminar(1L);
        mockMvc.perform(delete("/api/banners/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test @DisplayName("DELETE /api/banners/{id} → 404 si no existe")
    @WithMockUser(roles = "ADMIN")
    void eliminar_noExiste_retorna404() throws Exception {
        doThrow(new IllegalArgumentException("no encontrado"))
                .when(bannerService).eliminar(99L);
        mockMvc.perform(delete("/api/banners/99").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
