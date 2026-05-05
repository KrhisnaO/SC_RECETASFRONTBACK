package com.recetas.frontend.service;

import com.recetas.frontend.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApiClient - pruebas unitarias del cliente HTTP frontend")
class ApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    private ApiClient apiClient;

    @BeforeEach
    void setUp() {
        apiClient = new ApiClient(restTemplate, "http://localhost:8080");
    }

    // ── obtenerRecetas ───────────────────────────────────────────────────
    @Test
    @DisplayName("obtenerRecetas retorna lista cuando el backend responde OK")
    void obtenerRecetas_retornaLista() {
        RecetaDTO r = new RecetaDTO();
        r.setNombre("Tacos");
        ResponseEntity<List<RecetaDTO>> resp =
                new ResponseEntity<>(List.of(r), HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                isNull(), any(ParameterizedTypeReference.class))).thenReturn(resp);

        List<RecetaDTO> resultado = apiClient.obtenerRecetas();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Tacos");
    }

    @Test
    @DisplayName("obtenerRecetas retorna lista vacía si body es null")
    void obtenerRecetas_retornaVacioSiBodyNull() {
        ResponseEntity<List<RecetaDTO>> resp =
                new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                isNull(), any(ParameterizedTypeReference.class))).thenReturn(resp);

        assertThat(apiClient.obtenerRecetas()).isEmpty();
    }

    // ── obtenerReceta ────────────────────────────────────────────────────
    @Test
    @DisplayName("obtenerReceta retorna DTO si existe")
    void obtenerReceta_retornaDtoSiExiste() {
        RecetaDTO r = new RecetaDTO();
        r.setId(1L);
        when(restTemplate.getForObject(contains("/api/recetas/1"), eq(RecetaDTO.class))).thenReturn(r);

        RecetaDTO resultado = apiClient.obtenerReceta(1L);
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("obtenerReceta retorna null si el backend responde 404")
    void obtenerReceta_retornaNullSi404() {
        when(restTemplate.getForObject(anyString(), eq(RecetaDTO.class)))
                .thenThrow(HttpClientErrorException.NotFound.class);
        assertThat(apiClient.obtenerReceta(99L)).isNull();
    }

    // ── buscarRecetas ────────────────────────────────────────────────────
    @Test
    @DisplayName("buscarRecetas retorna lista con filtros")
    void buscarRecetas_retornaLista() {
        RecetaDTO r = new RecetaDTO();
        r.setNombre("Paella");
        ResponseEntity<List<RecetaDTO>> resp =
                new ResponseEntity<>(List.of(r), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                isNull(), any(ParameterizedTypeReference.class), any(), any(), any(), any()))
                .thenReturn(resp);

        List<RecetaDTO> resultado = apiClient.buscarRecetas("paella", null, null, null);
        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("buscarRecetas retorna vacío si body null")
    void buscarRecetas_retornaVacioSiNull() {
        ResponseEntity<List<RecetaDTO>> resp = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                isNull(), any(ParameterizedTypeReference.class), any(), any(), any(), any()))
                .thenReturn(resp);
        assertThat(apiClient.buscarRecetas(null, null, null, null)).isEmpty();
    }

    // ── obtenerComentarios ───────────────────────────────────────────────
    @Test
    @DisplayName("obtenerComentarios retorna lista")
    void obtenerComentarios_retornaLista() {
        ComentarioDTO c = new ComentarioDTO();
        c.setContenido("Excelente!");
        ResponseEntity<List<ComentarioDTO>> resp =
                new ResponseEntity<>(List.of(c), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                isNull(), any(ParameterizedTypeReference.class))).thenReturn(resp);

        List<ComentarioDTO> resultado = apiClient.obtenerComentarios(1L);
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getContenido()).isEqualTo("Excelente!");
    }

    // ── obtenerValoracion ────────────────────────────────────────────────
    @Test
    @DisplayName("obtenerValoracion retorna DTO con promedio y total")
    void obtenerValoracion_retornaDto() {
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(Map.of("promedio", 4.5, "total", 10));

        ValoracionDTO resultado = apiClient.obtenerValoracion(1L);
        assertThat(resultado.getPromedio()).isEqualTo(4.5);
        assertThat(resultado.getTotal()).isEqualTo(10L);
    }

    @Test
    @DisplayName("obtenerValoracion retorna 0 si el backend falla")
    void obtenerValoracion_retornaCeroSiFalla() {
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("Backend caído"));
        ValoracionDTO dto = apiClient.obtenerValoracion(1L);
        assertThat(dto.getPromedio()).isEqualTo(0.0);
        assertThat(dto.getTotal()).isEqualTo(0L);
    }

    // ── login ────────────────────────────────────────────────────────────
    @Test
    @DisplayName("login retorna token JWT si credenciales correctas")
    void login_retornaToken() {
        ResponseEntity<Map> resp = new ResponseEntity<>(
                Map.of("token", "eyJ.fake", "username", "admin"), HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(resp);

        String token = apiClient.login("admin", "password");
        assertThat(token).isEqualTo("eyJ.fake");
    }

    @Test
    @DisplayName("login retorna null si credenciales incorrectas (401)")
    void login_retornaNullSi401() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(HttpClientErrorException.Unauthorized.class);
        assertThat(apiClient.login("admin", "wrong")).isNull();
    }

    // ── publicarComentario ───────────────────────────────────────────────
    @Test
    @DisplayName("publicarComentario retorna true si backend responde 201")
    void publicarComentario_retornaTrue() {
        ResponseEntity<String> resp = new ResponseEntity<>("ok", HttpStatus.CREATED);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(resp);
        assertThat(apiClient.publicarComentario(1L, "Delicioso!", "token")).isTrue();
    }

    @Test
    @DisplayName("publicarComentario retorna false si lanza excepción")
    void publicarComentario_retornaFalseSiError() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Error"));
        assertThat(apiClient.publicarComentario(1L, "Texto", "token")).isFalse();
    }

    // ── valorar ──────────────────────────────────────────────────────────
    @Test
    @DisplayName("valorar retorna true si backend responde 200")
    void valorar_retornaTrue() {
        ResponseEntity<String> resp = new ResponseEntity<>("ok", HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(resp);
        assertThat(apiClient.valorar(1L, 5, "token")).isTrue();
    }

    @Test
    @DisplayName("valorar retorna false si lanza excepción")
    void valorar_retornaFalseSiError() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Error"));
        assertThat(apiClient.valorar(1L, 5, "token")).isFalse();
    }
}
