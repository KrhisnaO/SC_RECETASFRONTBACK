package com.recetas_back.recetas_back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecetaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.recetas_back.recetas_back.config.SecurityConfig.class)
@DisplayName("RecetaController REST - pruebas unitarias completas")
class RecetaControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
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
        receta.setIngredientes("Arroz, Mariscos");
        receta.setInstrucciones("Cocinar el arroz.");
        receta.setDescripcion("Clásica paella.");
        receta.setElementosMedia(new ArrayList<>());
        receta.setComentarios(new ArrayList<>());
        receta.setValoraciones(new ArrayList<>());
    }

    // ── GET ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/recetas → 200 con lista de recetas")
    void listarTodas_retorna200() throws Exception {
        when(recetaService.listarTodas()).thenReturn(List.of(receta));
        mockMvc.perform(get("/api/recetas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Paella"));
    }

    @Test
    @DisplayName("GET /api/recetas → 200 con lista vacía")
    void listarTodas_retornaVacio() throws Exception {
        when(recetaService.listarTodas()).thenReturn(List.of());
        mockMvc.perform(get("/api/recetas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/recetas/{id} → 200 si existe")
    void obtenerPorId_retorna200() throws Exception {
        when(recetaService.obtenerPorId(1L)).thenReturn(Optional.of(receta));
        mockMvc.perform(get("/api/recetas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Paella"));
    }

    @Test
    @DisplayName("GET /api/recetas/{id} → 404 si no existe")
    void obtenerPorId_retorna404() throws Exception {
        when(recetaService.obtenerPorId(anyLong())).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/recetas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/recetas/buscar → 200 con resultados")
    void buscar_retorna200() throws Exception {
        when(recetaService.buscar(any(), any(), any(), any())).thenReturn(List.of(receta));
        mockMvc.perform(get("/api/recetas/buscar").param("query", "paella"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/recetas/buscar → 200 sin resultados")
    void buscar_retornaVacio() throws Exception {
        when(recetaService.buscar(any(), any(), any(), any())).thenReturn(List.of());
        mockMvc.perform(get("/api/recetas/buscar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/recetas/buscar con todos los filtros → 200")
    void buscar_todosLosFiltros_200() throws Exception {
        when(recetaService.buscar(any(), any(), any(), any())).thenReturn(List.of(receta));
        mockMvc.perform(get("/api/recetas/buscar")
                        .param("query", "paella")
                        .param("tipoCocina", "Espanola")
                        .param("pais", "España")
                        .param("dificultad", "Media"))
                .andExpect(status().isOk());
    }

    // ── POST publicar ─────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/recetas → 201 con datos válidos y usuario autenticado")
    @WithMockUser(username = "chef")
    void publicar_datosValidos_retorna201() throws Exception {
        when(recetaService.crear(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any())).thenReturn(receta);

        String body = objectMapper.writeValueAsString(Map.of(
                "nombre", "Paella",
                "tipoCocina", "Espanola",
                "pais", "España",
                "dificultad", "Media",
                "tiempoPrepMinutos", 60,
                "descripcion", "Clásica",
                "instrucciones", "Cocinar arroz.",
                "ingredientes", "Arroz, Mariscos",
                "imagenUrl", "https://img.com/paella.jpg"
        ));

        mockMvc.perform(post("/api/recetas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/recetas → 400 si nombre es inválido")
    @WithMockUser(username = "chef")
    void publicar_nombreInvalido_retorna400() throws Exception {
        when(recetaService.crear(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("El nombre de la receta es obligatorio."));

        mockMvc.perform(post("/api/recetas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"tipoCocina\":\"Espanola\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/recetas → 201 sin Principal (filtros desactivados en test)")
    void publicar_sinPrincipal_usaFallback() throws Exception {
        when(recetaService.crear(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), eq("test"))).thenReturn(receta);

        mockMvc.perform(post("/api/recetas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Paella\",\"tiempoPrepMinutos\":60}"))
                .andExpect(status().isCreated());
    }

    // ── DELETE ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/recetas/{id} → 204 si la receta existe")
    @WithMockUser(roles = "ADMIN")
    void eliminar_existente_retorna204() throws Exception {
        doNothing().when(recetaService).eliminar(1L);

        mockMvc.perform(delete("/api/recetas/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/recetas/{id} → 404 si la receta no existe")
    @WithMockUser(roles = "ADMIN")
    void eliminar_noExiste_retorna404() throws Exception {
        doThrow(new RuntimeException("Receta no encontrada"))
                .when(recetaService).eliminar(99L);

        mockMvc.perform(delete("/api/recetas/99").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}