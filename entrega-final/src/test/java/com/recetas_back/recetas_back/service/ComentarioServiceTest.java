package com.recetas_back.recetas_back.service;

import com.recetas_back.recetas_back.exception.ContenidoNoPermitidoException;
import com.recetas_back.recetas_back.model.Comentario;
import com.recetas_back.recetas_back.model.Comentario.Estado;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ComentarioService - pruebas unitarias")
class ComentarioServiceTest {

    @Mock ComentarioRepository comentarioRepository;
    @Mock RecetaRepository recetaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock ModeracionService moderacionService;
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
        comentario.setEstado(Estado.APROBADO);
    }

    @Test @DisplayName("listarPorReceta solo retorna comentarios APROBADOS")
    void listarPorReceta_retornaSoloAprobados() {
        when(comentarioRepository.findByRecetaIdAndEstadoOrderByCreatedAtDesc(1L, Estado.APROBADO))
                .thenReturn(List.of(comentario));
        List<Comentario> result = comentarioService.listarPorReceta(1L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContenido()).isEqualTo("Excelente!");
    }

    @Test @DisplayName("listarPorReceta retorna vacío si no hay comentarios aprobados")
    void listarPorReceta_retornaVacio() {
        when(comentarioRepository.findByRecetaIdAndEstadoOrderByCreatedAtDesc(1L, Estado.APROBADO))
                .thenReturn(List.of());
        assertThat(comentarioService.listarPorReceta(1L)).isEmpty();
    }

    @Test @DisplayName("agregar guarda comentario como PENDIENTE si pasa moderación")
    void agregar_guardaComoPendiente() {
        when(recetaRepository.findById(1L)).thenReturn(Optional.of(receta));
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(usuario));
        when(moderacionService.motivoRechazoSiHay(anyString())).thenReturn(null);
        when(comentarioRepository.save(any(Comentario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Comentario result = comentarioService.agregar(1L, "maria", "Excelente!");
        assertThat(result.getContenido()).isEqualTo("Excelente!");
        assertThat(result.getEstado()).isEqualTo(Estado.PENDIENTE);
        verify(comentarioRepository).save(any(Comentario.class));
    }

    @Test @DisplayName("agregar lanza ContenidoNoPermitidoException si moderación rechaza")
    void agregar_rechazadoPorModeracion() {
        when(recetaRepository.findById(1L)).thenReturn(Optional.of(receta));
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(usuario));
        when(moderacionService.motivoRechazoSiHay(anyString()))
                .thenReturn("Contiene lenguaje no permitido (palabra prohibida).");

        assertThatThrownBy(() -> comentarioService.agregar(1L, "maria", "Texto ofensivo"))
                .isInstanceOf(ContenidoNoPermitidoException.class)
                .hasMessageContaining("no permitido");
        // El comentario rechazado igual se persiste como RECHAZADO para auditoría
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

    @Test @DisplayName("aprobar cambia el estado a APROBADO")
    void aprobar_cambiaEstado() {
        Comentario pendiente = new Comentario();
        pendiente.setId(5L); pendiente.setEstado(Estado.PENDIENTE);
        when(comentarioRepository.findById(5L)).thenReturn(Optional.of(pendiente));
        when(comentarioRepository.save(any(Comentario.class))).thenAnswer(inv -> inv.getArgument(0));

        Comentario result = comentarioService.aprobar(5L);
        assertThat(result.getEstado()).isEqualTo(Estado.APROBADO);
    }

    @Test @DisplayName("rechazar cambia el estado a RECHAZADO con motivo")
    void rechazar_cambiaEstadoYMotivo() {
        Comentario pendiente = new Comentario();
        pendiente.setId(7L); pendiente.setEstado(Estado.PENDIENTE);
        when(comentarioRepository.findById(7L)).thenReturn(Optional.of(pendiente));
        when(comentarioRepository.save(any(Comentario.class))).thenAnswer(inv -> inv.getArgument(0));

        Comentario result = comentarioService.rechazar(7L, "Lenguaje inapropiado");
        assertThat(result.getEstado()).isEqualTo(Estado.RECHAZADO);
        assertThat(result.getMotivoRechazo()).isEqualTo("Lenguaje inapropiado");
    }
}
