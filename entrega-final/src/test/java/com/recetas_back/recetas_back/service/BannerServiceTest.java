package com.recetas_back.recetas_back.service;

import com.recetas_back.recetas_back.model.Banner;
import com.recetas_back.recetas_back.repository.BannerRepository;
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
@DisplayName("BannerService - pruebas unitarias")
class BannerServiceTest {

    @Mock BannerRepository bannerRepository;
    @InjectMocks BannerService bannerService;

    private Banner banner;

    @BeforeEach
    void setUp() {
        banner = new Banner();
        banner.setId(1L);
        banner.setTitulo("Promo gastronómica");
        banner.setImagenUrl("https://example.com/banner.jpg");
        banner.setActivo(true);
        banner.setOrden(1);
    }

    @Test @DisplayName("listarActivos delega en findByActivoTrueOrderByOrdenAscIdAsc")
    void listarActivos_delegaEnRepo() {
        when(bannerRepository.findByActivoTrueOrderByOrdenAscIdAsc())
                .thenReturn(List.of(banner));
        List<Banner> result = bannerService.listarActivos();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitulo()).isEqualTo("Promo gastronómica");
    }

    @Test @DisplayName("listarTodos retorna todos")
    void listarTodos_retornaTodos() {
        when(bannerRepository.findAll()).thenReturn(List.of(banner));
        assertThat(bannerService.listarTodos()).hasSize(1);
    }

    @Test @DisplayName("crear lanza excepción si titulo está vacío")
    void crear_titulosVacio_lanzaExcepcion() {
        assertThatThrownBy(() ->
                bannerService.crear("", "Empresa", "https://img.com/x.png", null, true, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("título");
    }

    @Test @DisplayName("crear lanza excepción si imagenUrl está vacía")
    void crear_imagenUrlVacia_lanzaExcepcion() {
        assertThatThrownBy(() ->
                bannerService.crear("Título", "Empresa", "", null, true, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("imagen");
    }

    @Test @DisplayName("crear con datos válidos persiste banner")
    void crear_datosValidos_persiste() {
        when(bannerRepository.save(any(Banner.class))).thenAnswer(inv -> inv.getArgument(0));
        Banner result = bannerService.crear("Promo", "Empresa", "https://img/x.png",
                "https://link.com", true, 5);
        assertThat(result.getTitulo()).isEqualTo("Promo");
        assertThat(result.getEmpresa()).isEqualTo("Empresa");
        assertThat(result.getActivo()).isTrue();
        assertThat(result.getOrden()).isEqualTo(5);
    }

    @Test @DisplayName("crear con activo null usa true por defecto")
    void crear_activoNull_porDefectoTrue() {
        when(bannerRepository.save(any(Banner.class))).thenAnswer(inv -> inv.getArgument(0));
        Banner result = bannerService.crear("Promo", null, "https://img/x.png",
                null, null, null);
        assertThat(result.getActivo()).isTrue();
        assertThat(result.getOrden()).isEqualTo(0);
    }

    @Test @DisplayName("actualizar modifica solo campos provistos")
    void actualizar_camposParciales() {
        when(bannerRepository.findById(1L)).thenReturn(Optional.of(banner));
        when(bannerRepository.save(any(Banner.class))).thenAnswer(inv -> inv.getArgument(0));
        Banner actualizado = bannerService.actualizar(1L, "Nuevo título", null,
                null, null, false, null);
        assertThat(actualizado.getTitulo()).isEqualTo("Nuevo título");
        assertThat(actualizado.getActivo()).isFalse();
        assertThat(actualizado.getImagenUrl()).isEqualTo("https://example.com/banner.jpg");
    }

    @Test @DisplayName("actualizar lanza excepción si no existe")
    void actualizar_noExiste_excepcion() {
        when(bannerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() ->
                bannerService.actualizar(99L, "X", null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test @DisplayName("eliminar borra si existe")
    void eliminar_existente() {
        when(bannerRepository.existsById(1L)).thenReturn(true);
        bannerService.eliminar(1L);
        verify(bannerRepository).deleteById(1L);
    }

    @Test @DisplayName("eliminar lanza excepción si no existe")
    void eliminar_noExiste_excepcion() {
        when(bannerRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> bannerService.eliminar(99L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
