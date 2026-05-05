package com.recetas.frontend.service;

import com.recetas.frontend.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Cliente HTTP del FRONTEND que consume la API REST del BACKEND.
 *
 * Esta es la pieza clave de la separación: el frontend NO accede directamente
 * a la base de datos. Toda la comunicación pasa por HTTP hacia el backend.
 *
 * El backend corre en :8080, el frontend en :8081.
 */
@Service
public class ApiClient {

    private final RestTemplate restTemplate;

    @Value("${backend.base-url:http://localhost:8080}")
    private String backendUrl;

    public ApiClient() {
        this.restTemplate = new RestTemplate();
    }

    // Constructor para inyección en tests
    public ApiClient(RestTemplate restTemplate, String backendUrl) {
        this.restTemplate = restTemplate;
        this.backendUrl = backendUrl;
    }

    /** Obtiene todas las recetas del backend */
    public List<RecetaDTO> obtenerRecetas() {
        ResponseEntity<List<RecetaDTO>> resp = restTemplate.exchange(
                backendUrl + "/api/recetas",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<RecetaDTO>>() {});
        return resp.getBody() != null ? resp.getBody() : List.of();
    }

    /** Obtiene una receta por ID */
    public RecetaDTO obtenerReceta(Long id) {
        try {
            return restTemplate.getForObject(backendUrl + "/api/recetas/" + id, RecetaDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    /** Busca recetas con filtros opcionales */
    public List<RecetaDTO> buscarRecetas(String query, String tipoCocina, String pais, String dificultad) {
        String url = backendUrl + "/api/recetas/buscar?query={q}&tipoCocina={tc}&pais={p}&dificultad={d}";
        ResponseEntity<List<RecetaDTO>> resp = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<RecetaDTO>>() {},
                query != null ? query : "",
                tipoCocina != null ? tipoCocina : "",
                pais != null ? pais : "",
                dificultad != null ? dificultad : "");
        return resp.getBody() != null ? resp.getBody() : List.of();
    }

    /** Obtiene los comentarios de una receta */
    public List<ComentarioDTO> obtenerComentarios(Long recetaId) {
        ResponseEntity<List<ComentarioDTO>> resp = restTemplate.exchange(
                backendUrl + "/api/recetas/" + recetaId + "/comentarios",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<ComentarioDTO>>() {});
        return resp.getBody() != null ? resp.getBody() : List.of();
    }

    /** Obtiene el promedio de valoraciones de una receta */
    @SuppressWarnings("unchecked")
    public ValoracionDTO obtenerValoracion(Long recetaId) {
        try {
            Map<String,Object> resp = restTemplate.getForObject(
                    backendUrl + "/api/recetas/" + recetaId + "/valoraciones/promedio",
                    Map.class);
            ValoracionDTO dto = new ValoracionDTO();
            if (resp != null) {
                Object prom = resp.get("promedio");
                Object total = resp.get("total");
                dto.setPromedio(prom != null ? ((Number) prom).doubleValue() : 0.0);
                dto.setTotal(total != null ? ((Number) total).longValue() : 0L);
            }
            return dto;
        } catch (Exception e) {
            ValoracionDTO dto = new ValoracionDTO();
            dto.setPromedio(0.0);
            dto.setTotal(0L);
            return dto;
        }
    }

    /**
     * Autentica al usuario contra el backend.
     * @return token JWT o null si falló.
     */
    @SuppressWarnings("unchecked")
    public String login(String username, String password) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    backendUrl + "/api/auth/login", entity, Map.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return (String) resp.getBody().get("token");
            }
        } catch (HttpClientErrorException.Unauthorized e) {
            return null;
        }
        return null;
    }

    /**
     * Publica un comentario autenticado.
     * @param token JWT del usuario.
     */
    public boolean publicarComentario(Long recetaId, String contenido, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            String body = "{\"contenido\":\"" + contenido + "\"}";
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> resp = restTemplate.postForEntity(
                    backendUrl + "/api/recetas/" + recetaId + "/comentarios",
                    entity, String.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Envía una valoración autenticada.
     */
    public boolean valorar(Long recetaId, Integer puntuacion, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            String body = "{\"puntuacion\":" + puntuacion + "}";
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> resp = restTemplate.postForEntity(
                    backendUrl + "/api/recetas/" + recetaId + "/valoraciones",
                    entity, String.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}
