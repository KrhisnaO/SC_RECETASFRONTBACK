package com.recetas_back.recetas_back.service;

import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.model.Usuario;
import com.recetas_back.recetas_back.model.Valoracion;
import com.recetas_back.recetas_back.repository.RecetaRepository;
import com.recetas_back.recetas_back.repository.UsuarioRepository;
import com.recetas_back.recetas_back.repository.ValoracionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValoracionService - pruebas unitarias")
class ValoracionServiceTest {

    @Mock ValoracionRepository valoracionRepository;
    @Mock RecetaRepository recetaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks ValoracionService valoracionService;

    private Receta receta;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        receta = new Receta(); receta.setId(1L);
        usuario = new Usuario(); usuario.setId(1L); usuario.setUsername("carlos");
    }

    @Test @DisplayName("obtenerPromedio retorna valor del repositorio")
    void obtenerPromedio_retornaValor() {
        when(valoracionRepository.findAverageByRecetaId(1L)).thenReturn(Optional.of(4.5));
        assertThat(valoracionService.obtenerPromedio(1L)).isEqualTo(4.5);
    }

    @Test @DisplayName("obtenerPromedio retorna 0 si no hay valoraciones")
    void obtenerPromedio_retornaCero() {
        when(valoracionRepository.findAverageByRecetaId(1L)).thenReturn(Optional.empty());
        assertThat(valoracionService.obtenerPromedio(1L)).isEqualTo(0.0);
    }

    @Test @DisplayName("obtenerTotal retorna conteo del repositorio")
    void obtenerTotal_retornaConteo() {
        when(valoracionRepository.countByRecetaId(1L)).thenReturn(5L);
        assertThat(valoracionService.obtenerTotal(1L)).isEqualTo(5L);
    }

    @Test @DisplayName("guardarOActualizar crea nueva valoración")
    void guardarOActualizar_creaValoracionNueva() {
        when(recetaRepository.findById(1L)).thenReturn(Optional.of(receta));
        when(usuarioRepository.findByUsername("carlos")).thenReturn(Optional.of(usuario));
        when(valoracionRepository.findByRecetaIdAndUsuarioId(1L, 1L)).thenReturn(Optional.empty());
        Valoracion nueva = new Valoracion(); nueva.setPuntuacion(5);
        when(valoracionRepository.save(any(Valoracion.class))).thenReturn(nueva);

        Valoracion result = valoracionService.guardarOActualizar(1L, "carlos", 5);
        assertThat(result.getPuntuacion()).isEqualTo(5);
        verify(valoracionRepository).save(any(Valoracion.class));
    }

    @Test @DisplayName("guardarOActualizar actualiza valoración existente")
    void guardarOActualizar_actualizaExistente() {
        Valoracion existente = new Valoracion(); existente.setId(10L); existente.setPuntuacion(3);
        when(recetaRepository.findById(1L)).thenReturn(Optional.of(receta));
        when(usuarioRepository.findByUsername("carlos")).thenReturn(Optional.of(usuario));
        when(valoracionRepository.findByRecetaIdAndUsuarioId(1L, 1L)).thenReturn(Optional.of(existente));
        Valoracion actualizada = new Valoracion(); actualizada.setPuntuacion(5);
        when(valoracionRepository.save(existente)).thenReturn(actualizada);

        Valoracion result = valoracionService.guardarOActualizar(1L, "carlos", 5);
        assertThat(result.getPuntuacion()).isEqualTo(5);
    }

    @Test @DisplayName("guardarOActualizar lanza excepción si receta no existe")
    void guardarOActualizar_lanzaExcepcionReceta() {
        when(recetaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> valoracionService.guardarOActualizar(99L, "carlos", 4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Receta no encontrada");
    }

    @Test @DisplayName("guardarOActualizar lanza excepción si usuario no existe")
    void guardarOActualizar_lanzaExcepcionUsuario() {
        when(recetaRepository.findById(1L)).thenReturn(Optional.of(receta));
        when(usuarioRepository.findByUsername("x")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> valoracionService.guardarOActualizar(1L, "x", 4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuario no encontrado");
    }
}
