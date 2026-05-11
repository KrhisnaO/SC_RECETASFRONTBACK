package com.recetas_back.recetas_back.controller;

import com.recetas_back.recetas_back.model.Favorito;
import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.service.FavoritoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavoritoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.recetas_back.recetas_back.config.SecurityConfig.class)
@DisplayName("FavoritoController REST - pruebas unitarias")
class FavoritoControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean FavoritoService favoritoService;
    @MockBean com.recetas_back.recetas_back.security.JwtAuthenticationFilter jwtFilter;
    @MockBean com.recetas_back.recetas_back.security.CustomUserDetailsService userDetailsService;
    @MockBean org.springframework.security.authentication.AuthenticationManager authManager;

    private static Principal user(String name) {
        return () -> name;
    }

    private Receta receta;

    @BeforeEach
    void setUp() {
        receta = new Receta();
        receta.setId(1L);
        receta.setNombre("Sushi");
        receta.setTipoCocina("Asiatica");
    }

    @Test
    @DisplayName("GET /api/favoritos → 200 con lista de recetas favoritas")
    void listar_retornaLista() throws Exception {
        when(favoritoService.listar(anyString())).thenReturn(List.of(receta));

        mockMvc.perform(get("/api/favoritos").principal(user("maria")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Sushi"));
    }

    @Test
    @DisplayName("GET /api/favoritos → 200 con lista vacía")
    void listar_retornaVacio() throws Exception {
        when(favoritoService.listar(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/favoritos").principal(user("maria")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("POST /api/favoritos/{id} → 200 al agregar receta válida")
    void agregar_exitoso_retorna200() throws Exception {
        Favorito fav = new Favorito();
        fav.setReceta(receta);
        when(favoritoService.agregar(anyString(), eq(1L))).thenReturn(fav);

        mockMvc.perform(post("/api/favoritos/1").with(csrf()).principal(user("maria")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("POST /api/favoritos/{id} → 404 si receta no existe")
    void agregar_recetaNoExiste_retorna404() throws Exception {
        when(favoritoService.agregar(anyString(), anyLong()))
                .thenThrow(new IllegalArgumentException("Receta no encontrada"));

        mockMvc.perform(post("/api/favoritos/99").with(csrf()).principal(user("maria")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("DELETE /api/favoritos/{id} → 200 al eliminar favorito existente")
    void eliminar_exitoso_retorna200() throws Exception {
        doNothing().when(favoritoService).eliminar(anyString(), eq(1L));

        mockMvc.perform(delete("/api/favoritos/1").with(csrf()).principal(user("maria")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("DELETE /api/favoritos/{id} → 404 si usuario no existe")
    void eliminar_usuarioNoExiste_retorna404() throws Exception {
        doThrow(new IllegalArgumentException("Usuario no encontrado"))
                .when(favoritoService).eliminar(anyString(), anyLong());

        mockMvc.perform(delete("/api/favoritos/1").with(csrf()).principal(user("maria")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/favoritos/{id}/es → true si es favorito")
    void esFavorito_retornaTrue() throws Exception {
        when(favoritoService.esFavorito(anyString(), eq(1L))).thenReturn(true);

        mockMvc.perform(get("/api/favoritos/1/es").principal(user("maria")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esFavorito").value(true));
    }

    @Test
    @DisplayName("GET /api/favoritos/{id}/es → false si no es favorito")
    void esFavorito_retornaFalse() throws Exception {
        when(favoritoService.esFavorito(anyString(), eq(1L))).thenReturn(false);

        mockMvc.perform(get("/api/favoritos/1/es").principal(user("maria")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esFavorito").value(false));
    }
}
