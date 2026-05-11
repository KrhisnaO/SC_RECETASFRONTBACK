package com.recetas_back.recetas_back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recetas_back.recetas_back.model.Usuario;
import com.recetas_back.recetas_back.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.recetas_back.recetas_back.config.SecurityConfig.class)
@DisplayName("UsuarioController REST - pruebas unitarias")
class UsuarioControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean UsuarioRepository usuarioRepository;
    @MockBean PasswordEncoder passwordEncoder;
    @MockBean com.recetas_back.recetas_back.security.JwtAuthenticationFilter jwtFilter;
    @MockBean com.recetas_back.recetas_back.security.CustomUserDetailsService userDetailsService;
    @MockBean org.springframework.security.authentication.AuthenticationManager authManager;

    private static Authentication user(String name) {
        return new UsernamePasswordAuthenticationToken(name, "pwd");
    }

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("maria");
        usuario.setNombre("Maria Pérez");
        usuario.setCorreo("maria@test.com");
        usuario.setRole("ROLE_USER");
        usuario.setPassword("$2a$10$hashedPassword");
    }

    // ── GET /api/usuario/perfil ───────────────────────────────────────────

    @Test
    @DisplayName("GET /api/usuario/perfil → 200 con datos del perfil")
    void getPerfil_retorna200() throws Exception {
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/api/usuario/perfil").principal(user("maria")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("maria"))
                .andExpect(jsonPath("$.nombre").value("Maria Pérez"));
    }

    @Test
    @DisplayName("GET /api/usuario/perfil → 404 si usuario no existe")
    void getPerfil_retorna404() throws Exception {
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/usuario/perfil").principal(user("desconocido")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/usuario/perfil → 200 con campos null se muestran como vacío")
    void getPerfil_camposNullVacios() throws Exception {
        Usuario u = new Usuario();
        u.setId(2L);
        u.setUsername("vacio");
        when(usuarioRepository.findByUsername("vacio")).thenReturn(Optional.of(u));

        mockMvc.perform(get("/api/usuario/perfil").principal(user("vacio")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(""))
                .andExpect(jsonPath("$.correo").value(""));
    }

    // ── PUT /api/usuario/perfil ───────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/usuario/perfil → 200 al actualizar nombre y correo")
    void actualizarPerfil_retorna200() throws Exception {
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCorreo(anyString())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        mockMvc.perform(put("/api/usuario/perfil").with(csrf()).principal(user("maria"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("nombre", "Maria Nueva", "correo", "nuevo@test.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("PUT /api/usuario/perfil → 404 si usuario no existe")
    void actualizarPerfil_retorna404() throws Exception {
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/usuario/perfil").with(csrf()).principal(user("x"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"X\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/usuario/perfil → 409 si el correo ya está en uso")
    void actualizarPerfil_correoDuplicado_retorna409() throws Exception {
        Usuario otro = new Usuario();
        otro.setId(99L);
        otro.setCorreo("ocupado@test.com");
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCorreo("ocupado@test.com")).thenReturn(Optional.of(otro));

        mockMvc.perform(put("/api/usuario/perfil").with(csrf()).principal(user("maria"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"ocupado@test.com\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/usuario/perfil → 200 con cuerpo vacío (no cambia nada)")
    void actualizarPerfil_cuerpoVacio_retorna200() throws Exception {
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        mockMvc.perform(put("/api/usuario/perfil").with(csrf()).principal(user("maria"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/usuario/perfil → mismo correo del usuario actual no da 409")
    void actualizarPerfil_mismoCorreo_retorna200() throws Exception {
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCorreo("maria@test.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        mockMvc.perform(put("/api/usuario/perfil").with(csrf()).principal(user("maria"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"maria@test.com\"}"))
                .andExpect(status().isOk());
    }

    // ── PUT /api/usuario/cambiar-password ─────────────────────────────────

    @Test
    @DisplayName("PUT /api/usuario/cambiar-password → 200 si actual es correcta")
    void cambiarPassword_retorna200() throws Exception {
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("actualOk", usuario.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("nuevaSegura")).thenReturn("$2a$10$nueva");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        mockMvc.perform(put("/api/usuario/cambiar-password").with(csrf()).principal(user("maria"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("passwordActual", "actualOk", "passwordNueva", "nuevaSegura"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/usuario/cambiar-password → 404 si usuario no existe")
    void cambiarPassword_retorna404() throws Exception {
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/usuario/cambiar-password").with(csrf()).principal(user("x"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"passwordActual\":\"a\",\"passwordNueva\":\"b123456\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/usuario/cambiar-password → 400 si falta passwordActual")
    void cambiarPassword_faltaActual_retorna400() throws Exception {
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(usuario));

        mockMvc.perform(put("/api/usuario/cambiar-password").with(csrf()).principal(user("maria"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"passwordNueva\":\"nueva123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/usuario/cambiar-password → 401 si actual no coincide")
    void cambiarPassword_actualIncorrecta_retorna401() throws Exception {
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("malActual", usuario.getPassword())).thenReturn(false);

        mockMvc.perform(put("/api/usuario/cambiar-password").with(csrf()).principal(user("maria"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"passwordActual\":\"malActual\",\"passwordNueva\":\"nueva123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/usuario/cambiar-password → 400 si nueva es muy corta")
    void cambiarPassword_nuevaCorta_retorna400() throws Exception {
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("actualOk", usuario.getPassword())).thenReturn(true);

        mockMvc.perform(put("/api/usuario/cambiar-password").with(csrf()).principal(user("maria"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"passwordActual\":\"actualOk\",\"passwordNueva\":\"123\"}"))
                .andExpect(status().isBadRequest());
    }
}
