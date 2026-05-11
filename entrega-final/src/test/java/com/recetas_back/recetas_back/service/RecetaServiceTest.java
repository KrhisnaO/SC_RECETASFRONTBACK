package com.recetas_back.recetas_back.service;

import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.model.Usuario;
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
@DisplayName("RecetaService - pruebas unitarias completas")
class RecetaServiceTest {

    @Mock RecetaRepository recetaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks RecetaService recetaService;

    private Receta receta;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        receta = new Receta();
        receta.setId(1L);
        receta.setNombre("Tacos");
        receta.setTipoCocina("Mexicana");
        receta.setPais("México");
        receta.setDificultad("Facil");
        receta.setTiempoPrepMinutos(30);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("chef");
    }

    // ── listarTodas ───────────────────────────────────────────────────────

    @Test
    @DisplayName("listarTodas devuelve lista completa")
    void listarTodas_devuelveListaCompleta() {
        when(recetaRepository.findAll()).thenReturn(List.of(receta));
        List<Receta> result = recetaService.listarTodas();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Tacos");
        verify(recetaRepository).findAll();
    }

    @Test
    @DisplayName("listarTodas retorna vacío sin recetas")
    void listarTodas_retornaVacio() {
        when(recetaRepository.findAll()).thenReturn(List.of());
        assertThat(recetaService.listarTodas()).isEmpty();
    }

    // ── obtenerPorId ──────────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerPorId retorna receta existente")
    void obtenerPorId_retornaReceta() {
        when(recetaRepository.findById(1L)).thenReturn(Optional.of(receta));
        assertThat(recetaService.obtenerPorId(1L)).isPresent();
    }

    @Test
    @DisplayName("obtenerPorId retorna vacío si no existe")
    void obtenerPorId_retornaVacio() {
        when(recetaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(recetaService.obtenerPorId(99L)).isEmpty();
    }

    // ── buscar ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("buscar delega en repositorio con filtros")
    void buscar_delegaEnRepositorio() {
        when(recetaRepository.searchRecetas("taco", "Mexicana", "México", "Facil"))
                .thenReturn(List.of(receta));
        List<Receta> result = recetaService.buscar("taco", "Mexicana", "México", "Facil");
        assertThat(result).hasSize(1);
        verify(recetaRepository).searchRecetas("taco", "Mexicana", "México", "Facil");
    }

    @Test
    @DisplayName("buscar con parámetros nulos devuelve lista")
    void buscar_conNulos() {
        when(recetaRepository.searchRecetas(null, null, null, null))
                .thenReturn(List.of(receta));
        assertThat(recetaService.buscar(null, null, null, null)).isNotNull();
    }

    // ── guardar ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("guardar persiste y retorna receta")
    void guardar_persiste() {
        when(recetaRepository.save(receta)).thenReturn(receta);
        assertThat(recetaService.guardar(receta).getNombre()).isEqualTo("Tacos");
        verify(recetaRepository).save(receta);
    }

    // ── crear ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("crear: guarda receta con todos los campos correctamente")
    void crear_camposCompletos_guardaReceta() {
        when(usuarioRepository.findByUsername("chef")).thenReturn(Optional.of(usuario));
        when(recetaRepository.save(any(Receta.class))).thenAnswer(inv -> inv.getArgument(0));

        Receta resultado = recetaService.crear(
                "Tacos", "Mexicana", "México", "Facil",
                30, "Clásico", "Marinar y asar.", "Cerdo, Tortilla",
                "https://img.com/tacos.jpg", "chef"
        );

        assertThat(resultado.getNombre()).isEqualTo("Tacos");
        assertThat(resultado.getTipoCocina()).isEqualTo("Mexicana");
        assertThat(resultado.getTiempoPrepMinutos()).isEqualTo(30);
        verify(recetaRepository).save(any(Receta.class));
    }

    @Test
    @DisplayName("crear: asigna imagen por defecto si imagenUrl está vacía")
    void crear_imagenUrlVacia_asignaDefault() {
        when(usuarioRepository.findByUsername("chef")).thenReturn(Optional.of(usuario));
        when(recetaRepository.save(any(Receta.class))).thenAnswer(inv -> inv.getArgument(0));

        Receta resultado = recetaService.crear(
                "Pizza", "Italiana", "Italia", "Facil",
                20, "Rica pizza", "Hornear", "Harina, Queso",
                "", "chef"
        );

        assertThat(resultado.getImagenUrl()).isNotBlank();
        assertThat(resultado.getImagenUrl()).startsWith("https://");
    }

    @Test
    @DisplayName("crear: asigna tiempo 0 si tiempoPrep es null")
    void crear_tiempoPrepNull_asignaCero() {
        when(usuarioRepository.findByUsername("chef")).thenReturn(Optional.of(usuario));
        when(recetaRepository.save(any(Receta.class))).thenAnswer(inv -> inv.getArgument(0));

        Receta resultado = recetaService.crear(
                "Sopa", null, null, null,
                null, null, null, null,
                null, "chef"
        );

        assertThat(resultado.getTiempoPrepMinutos()).isEqualTo(0);
    }

    @Test
    @DisplayName("crear: lanza excepción si nombre es null")
    void crear_nombreNull_lanzaExcepcion() {
        assertThatThrownBy(() -> recetaService.crear(
                null, "Italiana", "Italia", "Facil",
                20, "Desc", "Instruc", "Harina",
                "url", "chef"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("obligatorio");
    }

    @Test
    @DisplayName("crear: lanza excepción si nombre está en blanco")
    void crear_nombreBlanco_lanzaExcepcion() {
        assertThatThrownBy(() -> recetaService.crear(
                "   ", "Italiana", "Italia", "Facil",
                20, "Desc", "Instruc", "Harina",
                "url", "chef"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("crear: lanza excepción si el usuario no existe")
    void crear_usuarioNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByUsername("noexiste")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recetaService.crear(
                "Paella", "Espanola", "España", "Alta",
                90, "Desc", "Instruc", "Arroz",
                "url", "noexiste"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Usuario no encontrado");
    }

    // ── eliminar ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar: llama a deleteById del repositorio")
    void eliminar_llamaDeleteById() {
        doNothing().when(recetaRepository).deleteById(5L);

        recetaService.eliminar(5L);

        verify(recetaRepository).deleteById(5L);
    }

    // ── listarRecientes / listarPopulares ─────────────────────────────────

    @Test
    @DisplayName("listarRecientes acota el límite y consulta el repositorio")
    void listarRecientes_acotaLimite() {
        when(recetaRepository.findRecientes(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(List.of(receta));
        List<Receta> r = recetaService.listarRecientes(5);
        assertThat(r).hasSize(1);
        verify(recetaRepository).findRecientes(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("listarRecientes con límite 0 usa mínimo 1")
    void listarRecientes_limiteCero_usaMinimo() {
        when(recetaRepository.findRecientes(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(List.of());
        recetaService.listarRecientes(0);
        verify(recetaRepository).findRecientes(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("listarRecientes con límite excesivo se acota a 50")
    void listarRecientes_limiteExcesivo_seAcota() {
        when(recetaRepository.findRecientes(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(List.of());
        recetaService.listarRecientes(1000);
        verify(recetaRepository).findRecientes(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("listarPopulares delega en findPopulares del repositorio")
    void listarPopulares_delega() {
        when(recetaRepository.findPopulares(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(List.of(receta));
        assertThat(recetaService.listarPopulares(8)).hasSize(1);
        verify(recetaRepository).findPopulares(any(org.springframework.data.domain.Pageable.class));
    }
}