package com.recetas_back.recetas_back.service;

import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.repository.RecetaRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecetaService - pruebas unitarias")
class RecetaServiceTest {

    @Mock RecetaRepository recetaRepository;
    @InjectMocks RecetaService recetaService;

    private Receta receta;

    @BeforeEach
    void setUp() {
        receta = new Receta();
        receta.setId(1L); receta.setNombre("Tacos");
        receta.setTipoCocina("Mexicana"); receta.setPais("México");
        receta.setDificultad("Facil"); receta.setTiempoPrepMinutos(30);
    }

    @Test @DisplayName("listarTodas devuelve lista completa")
    void listarTodas_devuelveListaCompleta() {
        when(recetaRepository.findAll()).thenReturn(List.of(receta));
        List<Receta> result = recetaService.listarTodas();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Tacos");
        verify(recetaRepository).findAll();
    }

    @Test @DisplayName("listarTodas retorna vacío sin recetas")
    void listarTodas_retornaVacio() {
        when(recetaRepository.findAll()).thenReturn(List.of());
        assertThat(recetaService.listarTodas()).isEmpty();
    }

    @Test @DisplayName("obtenerPorId retorna receta existente")
    void obtenerPorId_retornaReceta() {
        when(recetaRepository.findById(1L)).thenReturn(Optional.of(receta));
        assertThat(recetaService.obtenerPorId(1L)).isPresent();
    }

    @Test @DisplayName("obtenerPorId retorna vacío si no existe")
    void obtenerPorId_retornaVacio() {
        when(recetaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(recetaService.obtenerPorId(99L)).isEmpty();
    }

    @Test @DisplayName("buscar delega en repositorio con filtros")
    void buscar_delegaEnRepositorio() {
        when(recetaRepository.searchRecetas("taco","Mexicana","México","Facil")).thenReturn(List.of(receta));
        List<Receta> result = recetaService.buscar("taco","Mexicana","México","Facil");
        assertThat(result).hasSize(1);
        verify(recetaRepository).searchRecetas("taco","Mexicana","México","Facil");
    }

    @Test @DisplayName("buscar con parámetros nulos devuelve lista")
    void buscar_conNulos() {
        when(recetaRepository.searchRecetas(null,null,null,null)).thenReturn(List.of(receta));
        assertThat(recetaService.buscar(null,null,null,null)).isNotNull();
    }

    @Test @DisplayName("guardar persiste y retorna receta")
    void guardar_persiste() {
        when(recetaRepository.save(receta)).thenReturn(receta);
        assertThat(recetaService.guardar(receta).getNombre()).isEqualTo("Tacos");
        verify(recetaRepository).save(receta);
    }
}
