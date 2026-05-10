package com.recetas_back.recetas_back.service;

import com.recetas_back.recetas_back.model.Comentario;
import com.recetas_back.recetas_back.model.Usuario;
import com.recetas_back.recetas_back.repository.ComentarioRepository;
import com.recetas_back.recetas_back.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AdminService - pruebas unitarias")
class AdminServiceTest {

    private AdminService adminService;
    private UsuarioRepository usuarioRepository;
    private ComentarioRepository comentarioRepository;

    @BeforeEach
    void setUp() {
        usuarioRepository    = mock(UsuarioRepository.class);
        comentarioRepository = mock(ComentarioRepository.class);

        adminService = new AdminService();
        ReflectionTestUtils.setField(adminService, "usuarioRepository", usuarioRepository);
        ReflectionTestUtils.setField(adminService, "comentarioRepository", comentarioRepository);
    }

    @Test
    @DisplayName("listarUsuarios: retorna todos los usuarios")
    void listarUsuarios() {
        Usuario u1 = new Usuario(); u1.setUsername("admin");
        Usuario u2 = new Usuario(); u2.setUsername("user1");
        when(usuarioRepository.findAll()).thenReturn(List.of(u1, u2));

        List<Usuario> resultado = adminService.listarUsuarios();

        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("cambiarRol: actualiza el rol correctamente")
    void cambiarRol_exitoso() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setUsername("user1");
        u.setRole("ROLE_USER");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = adminService.cambiarRol(1L, "ROLE_ADMIN");

        assertThat(resultado.getRole()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("cambiarRol: lanza excepción si usuario no existe")
    void cambiarRol_usuarioNoExiste() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.cambiarRol(99L, "ROLE_ADMIN"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("eliminarUsuario: llama a deleteById si existe")
    void eliminarUsuario_exitoso() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        adminService.eliminarUsuario(1L);

        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminarUsuario: lanza excepción si no existe")
    void eliminarUsuario_noExiste() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> adminService.eliminarUsuario(99L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("listarTodosComentarios: retorna lista completa")
    void listarTodosComentarios() {
        Comentario c1 = new Comentario(); c1.setContenido("Buena receta");
        Comentario c2 = new Comentario(); c2.setContenido("Muy fácil");
        when(comentarioRepository.findAll()).thenReturn(List.of(c1, c2));

        List<Comentario> resultado = adminService.listarTodosComentarios();

        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("eliminarComentario: llama a deleteById si existe")
    void eliminarComentario_exitoso() {
        when(comentarioRepository.existsById(5L)).thenReturn(true);

        adminService.eliminarComentario(5L);

        verify(comentarioRepository).deleteById(5L);
    }

    @Test
    @DisplayName("eliminarComentario: lanza excepción si no existe")
    void eliminarComentario_noExiste() {
        when(comentarioRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> adminService.eliminarComentario(99L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}