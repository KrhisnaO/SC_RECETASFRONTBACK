package com.recetas_back.recetas_back.service;

import com.recetas_back.recetas_back.model.Favorito;
import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.model.Usuario;
import com.recetas_back.recetas_back.repository.FavoritoRepository;
import com.recetas_back.recetas_back.repository.RecetaRepository;
import com.recetas_back.recetas_back.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("FavoritoService - pruebas unitarias")
class FavoritoServiceTest {

    private FavoritoService favoritoService;
    private FavoritoRepository favoritoRepository;
    private UsuarioRepository usuarioRepository;
    private RecetaRepository recetaRepository;

    private Usuario usuario;
    private Receta receta;

    @BeforeEach
    void setUp() {
        favoritoRepository  = mock(FavoritoRepository.class);
        usuarioRepository   = mock(UsuarioRepository.class);
        recetaRepository    = mock(RecetaRepository.class);

        favoritoService = new FavoritoService();
        ReflectionTestUtils.setField(favoritoService, "favoritoRepository", favoritoRepository);
        ReflectionTestUtils.setField(favoritoService, "usuarioRepository", usuarioRepository);
        ReflectionTestUtils.setField(favoritoService, "recetaRepository", recetaRepository);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("juan");

        receta = new Receta();
        receta.setId(10L);
        receta.setNombre("Tacos");
    }

    @Test
    @DisplayName("agregar: crea un nuevo favorito si no existía")
    void agregar_nuevoFavorito() {
        when(usuarioRepository.findByUsername("juan")).thenReturn(Optional.of(usuario));
        when(recetaRepository.findById(10L)).thenReturn(Optional.of(receta));
        when(favoritoRepository.findByUsuarioIdAndRecetaId(1L, 10L)).thenReturn(Optional.empty());

        Favorito guardado = new Favorito();
        guardado.setUsuario(usuario);
        guardado.setReceta(receta);
        when(favoritoRepository.save(any())).thenReturn(guardado);

        Favorito resultado = favoritoService.agregar("juan", 10L);

        assertThat(resultado.getReceta().getNombre()).isEqualTo("Tacos");
        verify(favoritoRepository).save(any());
    }

    @Test
    @DisplayName("agregar: idempotente si ya es favorito")
    void agregar_yaEsFavorito_noDuplica() {
        Favorito existente = new Favorito();
        existente.setUsuario(usuario);
        existente.setReceta(receta);

        when(usuarioRepository.findByUsername("juan")).thenReturn(Optional.of(usuario));
        when(recetaRepository.findById(10L)).thenReturn(Optional.of(receta));
        when(favoritoRepository.findByUsuarioIdAndRecetaId(1L, 10L)).thenReturn(Optional.of(existente));

        favoritoService.agregar("juan", 10L);

        verify(favoritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("eliminar: borra el favorito si existe")
    void eliminar_existente() {
        Favorito fav = new Favorito();
        when(usuarioRepository.findByUsername("juan")).thenReturn(Optional.of(usuario));
        when(favoritoRepository.findByUsuarioIdAndRecetaId(1L, 10L)).thenReturn(Optional.of(fav));

        favoritoService.eliminar("juan", 10L);

        verify(favoritoRepository).delete(fav);
    }

    @Test
    @DisplayName("listar: retorna lista de recetas favoritas")
    void listar_retornaRecetas() {
        Favorito fav = new Favorito();
        fav.setReceta(receta);

        when(usuarioRepository.findByUsername("juan")).thenReturn(Optional.of(usuario));
        when(favoritoRepository.findByUsuarioId(1L)).thenReturn(List.of(fav));

        List<Receta> resultado = favoritoService.listar("juan");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Tacos");
    }

    @Test
    @DisplayName("esFavorito: retorna true cuando existe el favorito")
    void esFavorito_retornaTrue() {
        when(usuarioRepository.findByUsername("juan")).thenReturn(Optional.of(usuario));
        when(favoritoRepository.findByUsuarioIdAndRecetaId(1L, 10L))
                .thenReturn(Optional.of(new Favorito()));

        assertThat(favoritoService.esFavorito("juan", 10L)).isTrue();
    }

    @Test
    @DisplayName("esFavorito: retorna false cuando no existe")
    void esFavorito_retornaFalse() {
        when(usuarioRepository.findByUsername("juan")).thenReturn(Optional.of(usuario));
        when(favoritoRepository.findByUsuarioIdAndRecetaId(1L, 10L)).thenReturn(Optional.empty());

        assertThat(favoritoService.esFavorito("juan", 10L)).isFalse();
    }
}