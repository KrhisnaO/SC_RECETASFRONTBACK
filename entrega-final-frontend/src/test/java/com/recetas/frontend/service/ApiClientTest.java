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
    @DisplayName("login retorna LoginResult si credenciales correctas")
    void login_retornaLoginResult() {
        ResponseEntity<Map> resp = new ResponseEntity<>(
                Map.of("token", "eyJ.fake", "role", "ROLE_USER"), HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(resp);

        ApiClient.LoginResult result = apiClient.login("admin", "password");
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("eyJ.fake");
        assertThat(result.role()).isEqualTo("ROLE_USER");
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

    // ── obtenerRecientes / obtenerPopulares ──────────────────────────────

    @Test
    @DisplayName("obtenerRecientes retorna lista del backend")
    void obtenerRecientes_retornaLista() {
        RecetaDTO r = new RecetaDTO(); r.setNombre("Nueva");
        ResponseEntity<List<RecetaDTO>> resp = new ResponseEntity<>(List.of(r), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                isNull(), any(ParameterizedTypeReference.class))).thenReturn(resp);

        List<RecetaDTO> resultado = apiClient.obtenerRecientes(8);
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Nueva");
    }

    @Test
    @DisplayName("obtenerRecientes retorna vacío si lanza excepción")
    void obtenerRecientes_retornaVacioSiError() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("Backend caído"));
        assertThat(apiClient.obtenerRecientes(8)).isEmpty();
    }

    @Test
    @DisplayName("obtenerPopulares retorna lista del backend")
    void obtenerPopulares_retornaLista() {
        RecetaDTO r = new RecetaDTO(); r.setNombre("Popular");
        ResponseEntity<List<RecetaDTO>> resp = new ResponseEntity<>(List.of(r), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                isNull(), any(ParameterizedTypeReference.class))).thenReturn(resp);
        assertThat(apiClient.obtenerPopulares(5)).hasSize(1);
    }

    // ── Banners ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerBanners retorna lista de banners")
    void obtenerBanners_retornaLista() {
        BannerDTO b = new BannerDTO(); b.setTitulo("Promo");
        ResponseEntity<List<BannerDTO>> resp = new ResponseEntity<>(List.of(b), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                isNull(), any(ParameterizedTypeReference.class))).thenReturn(resp);
        assertThat(apiClient.obtenerBanners()).hasSize(1);
    }

    @Test
    @DisplayName("obtenerBanners retorna vacío si lanza excepción")
    void obtenerBanners_retornaVacioSiError() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("Error"));
        assertThat(apiClient.obtenerBanners()).isEmpty();
    }

    @Test
    @DisplayName("crearBannerAdmin retorna true si backend responde 201")
    void crearBannerAdmin_retornaTrue() {
        ResponseEntity<String> resp = new ResponseEntity<>("ok", HttpStatus.CREATED);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(resp);
        assertThat(apiClient.crearBannerAdmin("T", "E", "https://img/x", "https://link",
                true, 1, "token")).isTrue();
    }

    @Test
    @DisplayName("eliminarBannerAdmin retorna true si backend responde 204")
    void eliminarBannerAdmin_retornaTrue() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE),
                any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        assertThat(apiClient.eliminarBannerAdmin(1L, "token")).isTrue();
    }

    @Test
    @DisplayName("eliminarBannerAdmin retorna false si lanza excepción")
    void eliminarBannerAdmin_retornaFalseSiError() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE),
                any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Error"));
        assertThat(apiClient.eliminarBannerAdmin(1L, "token")).isFalse();
    }

    // ── Moderación ───────────────────────────────────────────────────────

    @Test
    @DisplayName("publicarComentarioDetallado retorna OK si backend responde 201")
    void publicarComentarioDetallado_ok() {
        ResponseEntity<Map> resp = new ResponseEntity<>(Map.of("id", 1), HttpStatus.CREATED);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(resp);
        ApiClient.ComentarioResult r = apiClient.publicarComentarioDetallado(1L, "Texto", "token");
        assertThat(r.status()).isEqualTo("OK");
    }

    @Test
    @DisplayName("publicarComentarioDetallado retorna ERROR si conexión falla")
    void publicarComentarioDetallado_error() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Conexión rota"));
        ApiClient.ComentarioResult r = apiClient.publicarComentarioDetallado(1L, "Texto", "token");
        assertThat(r.status()).isEqualTo("ERROR");
    }

    @Test
    @DisplayName("aprobarComentarioAdmin retorna true si backend responde 200")
    void aprobarComentarioAdmin_retornaTrue() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT),
                any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        assertThat(apiClient.aprobarComentarioAdmin(1L, "token")).isTrue();
    }

    @Test
    @DisplayName("rechazarComentarioAdmin retorna true si backend responde 200")
    void rechazarComentarioAdmin_retornaTrue() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT),
                any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        assertThat(apiClient.rechazarComentarioAdmin(1L, "Lenguaje ofensivo", "token")).isTrue();
    }

    @Test
    @DisplayName("rechazarComentarioAdmin retorna false si lanza excepción")
    void rechazarComentarioAdmin_retornaFalseSiError() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT),
                any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Backend caído"));
        assertThat(apiClient.rechazarComentarioAdmin(1L, "x", "token")).isFalse();
    }

    @Test
    @DisplayName("listarComentariosPorEstado retorna lista filtrada")
    void listarComentariosPorEstado_retornaLista() {
        ResponseEntity<List<Map<String, Object>>> resp = new ResponseEntity<>(
                List.of(Map.of("id", 1, "estado", "PENDIENTE")), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(resp);
        assertThat(apiClient.listarComentariosPorEstado("PENDIENTE", "token")).hasSize(1);
    }
}
