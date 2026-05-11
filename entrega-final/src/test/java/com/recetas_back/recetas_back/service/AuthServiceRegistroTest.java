package com.recetas_back.recetas_back.service;

import com.recetas_back.recetas_back.dto.RegisterRequest;
import com.recetas_back.recetas_back.model.Usuario;
import com.recetas_back.recetas_back.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthService - registro de usuarios")
class AuthServiceRegistroTest {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder    = mock(PasswordEncoder.class);

        authService = new AuthService();

        // Inyectamos los mocks vía ReflectionTestUtils
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "usuarioRepository", usuarioRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(authService, "passwordEncoder", passwordEncoder);
    }

    @Test
    @DisplayName("registrar: crea usuario con contraseña encriptada y ROLE_USER")
    void registrar_usuarioNuevo_exitoso() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("nuevo_user");
        req.setEmail("nuevo@correo.com");
        req.setPassword("clave123");

        when(usuarioRepository.findByUsername("nuevo_user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("clave123")).thenReturn("$2a$hashed");

        Usuario guardado = new Usuario();
        guardado.setId(1L);
        guardado.setUsername("nuevo_user");
        guardado.setPassword("$2a$hashed");
        guardado.setRole("ROLE_USER");

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(guardado);

        Usuario resultado = authService.registrar(req);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getUsername()).isEqualTo("nuevo_user");
        assertThat(resultado.getRole()).isEqualTo("ROLE_USER");
        // La contraseña en texto plano NUNCA debe quedar sin encriptar
        assertThat(resultado.getPassword()).isNotEqualTo("clave123");
        verify(passwordEncoder).encode("clave123");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("registrar: lanza excepción si el username ya existe")
    void registrar_usernameExistente_lanzaExcepcion() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("admin");
        req.setPassword("algo");

        Usuario existente = new Usuario();
        existente.setUsername("admin");
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> authService.registrar(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya esta en uso");

        verify(usuarioRepository, never()).save(any());
    }
}