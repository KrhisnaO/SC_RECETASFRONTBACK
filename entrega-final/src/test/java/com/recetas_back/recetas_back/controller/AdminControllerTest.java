package com.recetas_back.recetas_back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recetas_back.recetas_back.model.Comentario;
import com.recetas_back.recetas_back.model.Usuario;
import com.recetas_back.recetas_back.service.AdminService;
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

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.recetas_back.recetas_back.config.SecurityConfig.class)
@DisplayName("AdminController REST - pruebas unitarias")
class AdminControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AdminService adminService;
    @MockBean com.recetas_back.recetas_back.security.JwtAuthenticationFilter jwtFilter;
    @MockBean com.recetas_back.recetas_back.security.CustomUserDetailsService userDetailsService;
    @MockBean org.springframework.security.authentication.AuthenticationManager authManager;

    private Usuario usuario;
    private Comentario comentario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("maria");
        usuario.setRole("ROLE_USER");

        comentario = new Comentario();
        comentario.setId(10L);
        comentario.setContenido("Comentario ofensivo");
    }

    // ── Usuarios ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/admin/usuarios → 200 con lista de usuarios")
    @WithMockUser(roles = "ADMIN")
    void listarUsuarios_retornaLista() throws Exception {
        when(adminService.listarUsuarios()).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("maria"));
    }

    @Test
    @DisplayName("GET /api/admin/usuarios → 200 con lista vacía")
    @WithMockUser(roles = "ADMIN")
    void listarUsuarios_retornaVacio() throws Exception {
        when(adminService.listarUsuarios()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("PUT /api/admin/usuarios/{id}/rol → 200 al cambiar rol")
    @WithMockUser(roles = "ADMIN")
    void cambiarRol_exitoso_retorna200() throws Exception {
        usuario.setRole("ROLE_ADMIN");
        when(adminService.cambiarRol(1L, "ROLE_ADMIN")).thenReturn(usuario);

        mockMvc.perform(put("/api/admin/usuarios/1/rol")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("PUT /api/admin/usuarios/{id}/rol → 400 si role es nulo")
    @WithMockUser(roles = "ADMIN")
    void cambiarRol_rolNulo_retorna400() throws Exception {
        mockMvc.perform(put("/api/admin/usuarios/1/rol")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/admin/usuarios/{id}/rol → 404 si usuario no existe")
    @WithMockUser(roles = "ADMIN")
    void cambiarRol_usuarioNoExiste_retorna404() throws Exception {
        when(adminService.cambiarRol(anyLong(), anyString()))
                .thenThrow(new IllegalArgumentException("Usuario no encontrado"));

        mockMvc.perform(put("/api/admin/usuarios/99/rol")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/admin/usuarios/{id} → 204 si existe")
    @WithMockUser(roles = "ADMIN")
    void eliminarUsuario_exitoso_retorna204() throws Exception {
        doNothing().when(adminService).eliminarUsuario(1L);

        mockMvc.perform(delete("/api/admin/usuarios/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/admin/usuarios/{id} → 404 si no existe")
    @WithMockUser(roles = "ADMIN")
    void eliminarUsuario_noExiste_retorna404() throws Exception {
        doThrow(new IllegalArgumentException("Usuario no encontrado"))
                .when(adminService).eliminarUsuario(99L);

        mockMvc.perform(delete("/api/admin/usuarios/99").with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ── Comentarios ───────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/admin/comentarios → 200 con lista de comentarios")
    @WithMockUser(roles = "ADMIN")
    void listarComentarios_retornaLista() throws Exception {
        when(adminService.listarTodosComentarios()).thenReturn(List.of(comentario));

        mockMvc.perform(get("/api/admin/comentarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contenido").value("Comentario ofensivo"));
    }

    @Test
    @DisplayName("GET /api/admin/comentarios → 200 con lista vacía")
    @WithMockUser(roles = "ADMIN")
    void listarComentarios_retornaVacio() throws Exception {
        when(adminService.listarTodosComentarios()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/comentarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("DELETE /api/admin/comentarios/{id} → 204 si existe")
    @WithMockUser(roles = "ADMIN")
    void eliminarComentario_exitoso_retorna204() throws Exception {
        doNothing().when(adminService).eliminarComentario(10L);

        mockMvc.perform(delete("/api/admin/comentarios/10").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/admin/comentarios/{id} → 404 si no existe")
    @WithMockUser(roles = "ADMIN")
    void eliminarComentario_noExiste_retorna404() throws Exception {
        doThrow(new IllegalArgumentException("Comentario no encontrado"))
                .when(adminService).eliminarComentario(99L);

        mockMvc.perform(delete("/api/admin/comentarios/99").with(csrf()))
                .andExpect(status().isNotFound());
    }
}