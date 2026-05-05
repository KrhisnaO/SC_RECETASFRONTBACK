package com.recetas_back.recetas_back.service;

import com.recetas_back.recetas_back.model.Comentario;
import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.model.Usuario;
import com.recetas_back.recetas_back.repository.ComentarioRepository;
import com.recetas_back.recetas_back.repository.RecetaRepository;
import com.recetas_back.recetas_back.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ComentarioService - pruebas unitarias")
class ComentarioServiceTest {

    @Mock ComentarioRepository comentarioRepository;
    @Mock RecetaRepository recetaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks ComentarioService comentarioService;

    private Receta receta;
    private Usuario usuario;
    private Comentario comentario;

    @BeforeEach
    void setUp() {
        receta = new Receta(); receta.setId(1L); receta.setNombre("Paella");
        usuario = new Usuario(); usuario.setId(1L); usuario.setUsername("maria");
        comentario = new Comentario();
        comentario.setId(1L); comentario.setContenido("Excelente!");
        comentario.setReceta(receta); comentario.setUsuario(usuario);
    }

    @Test @DisplayName("listarPorReceta retorna lista del repositorio")
    void listarPorReceta_retornaLista() {
        when(comentarioRepository.findByRecetaIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(comentario));
        List<Comentario> result = comentarioService.listarPorReceta(1L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContenido()).isEqualTo("Excelente!");
    }

    @Test @DisplayName("listarPorReceta retorna vacío si no hay comentarios")
    void listarPorReceta_retornaVacio() {
        when(comentarioRepository.findByRecetaIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        assertThat(comentarioService.listarPorReceta(1L)).isEmpty();
    }

    @Test @DisplayName("agregar guarda comentario correctamente")
    void agregar_guardaComentario() {
        when(recetaRepository.findById(1L)).thenReturn(Optional.of(receta));
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(usuario));
        when(comentarioRepository.save(any(Comentario.class))).thenReturn(comentario);

        Comentario result = comentarioService.agregar(1L, "maria", "Excelente!");
        assertThat(result.getContenido()).isEqualTo("Excelente!");
        verify(comentarioRepository).save(any(Comentario.class));
    }

    @Test @DisplayName("agregar lanza excepción si receta no existe")
    void agregar_lanzaExcepcionSiRecetaNoExiste() {
        when(recetaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> comentarioService.agregar(99L, "maria", "Texto"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Receta no encontrada");
    }

    @Test @DisplayName("agregar lanza excepción si usuario no existe")
    void agregar_lanzaExcepcionSiUsuarioNoExiste() {
        when(recetaRepository.findById(1L)).thenReturn(Optional.of(receta));
        when(usuarioRepository.findByUsername("x")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> comentarioService.agregar(1L, "x", "Texto"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuario no encontrado");
    }
}
