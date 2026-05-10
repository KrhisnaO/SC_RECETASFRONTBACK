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

@Service
public class ApiClient {

    public record LoginResult(String token, String role) {}

    private final RestTemplate restTemplate;

    @Value("${backend.base-url:http://localhost:8080}")
    private String backendUrl;

    public ApiClient() { this.restTemplate = new RestTemplate(); }

    public ApiClient(RestTemplate restTemplate, String backendUrl) {
        this.restTemplate = restTemplate;
        this.backendUrl   = backendUrl;
    }

    // Recetas

    public List<RecetaDTO> obtenerRecetas() {
        ResponseEntity<List<RecetaDTO>> resp = restTemplate.exchange(
                backendUrl + "/api/recetas", HttpMethod.GET, null,
                new ParameterizedTypeReference<List<RecetaDTO>>() {});
        return resp.getBody() != null ? resp.getBody() : List.of();
    }

    public RecetaDTO obtenerReceta(Long id) {
        try { return restTemplate.getForObject(backendUrl + "/api/recetas/" + id, RecetaDTO.class); }
        catch (HttpClientErrorException.NotFound e) { return null; }
    }

    public List<RecetaDTO> buscarRecetas(String query, String tipoCocina, String pais, String dificultad) {
        String url = backendUrl + "/api/recetas/buscar?query={q}&tipoCocina={tc}&pais={p}&dificultad={d}";
        ResponseEntity<List<RecetaDTO>> resp = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<RecetaDTO>>() {},
                q(query), q(tipoCocina), q(pais), q(dificultad));
        return resp.getBody() != null ? resp.getBody() : List.of();
    }

    @SuppressWarnings("unchecked")
    public String publicarReceta(String nombre, String tipoCocina, String pais, String dificultad,
                                  Integer tiempoPrep, String descripcion, String instrucciones,
                                  String ingredientes, String imagenUrl, String token) {
        try {
            String body = String.format(
                "{\"nombre\":\"%s\",\"tipoCocina\":\"%s\",\"pais\":\"%s\",\"dificultad\":\"%s\"," +
                "\"tiempoPrepMinutos\":%d,\"descripcion\":\"%s\",\"instrucciones\":\"%s\"," +
                "\"ingredientes\":\"%s\",\"imagenUrl\":\"%s\"}",
                esc(nombre), esc(q(tipoCocina)), esc(q(pais)), esc(q(dificultad)),
                tiempoPrep != null ? tiempoPrep : 0,
                esc(q(descripcion)), esc(q(instrucciones)), esc(q(ingredientes)), esc(q(imagenUrl)));
            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    backendUrl + "/api/recetas", jsonEntity(body, token), Map.class);
            return resp.getStatusCode().is2xxSuccessful() ? null : "Error al publicar la receta.";
        } catch (HttpClientErrorException e) {
            return "Error al publicar: " + e.getStatusCode();
        } catch (Exception e) {
            return "No se pudo conectar con el servidor.";
        }
    }

    // Comentarios

    public List<ComentarioDTO> obtenerComentarios(Long recetaId) {
        ResponseEntity<List<ComentarioDTO>> resp = restTemplate.exchange(
                backendUrl + "/api/recetas/" + recetaId + "/comentarios",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<ComentarioDTO>>() {});
        return resp.getBody() != null ? resp.getBody() : List.of();
    }

    public boolean publicarComentario(Long recetaId, String contenido, String token) {
        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(
                    backendUrl + "/api/recetas/" + recetaId + "/comentarios",
                    jsonEntity("{\"contenido\":\"" + esc(contenido) + "\"}", token), String.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception e) { return false; }
    }

    // Valoraciones

    @SuppressWarnings("unchecked")
    public ValoracionDTO obtenerValoracion(Long recetaId) {
        try {
            Map<String, Object> resp = restTemplate.getForObject(
                    backendUrl + "/api/recetas/" + recetaId + "/valoraciones/promedio", Map.class);
            ValoracionDTO dto = new ValoracionDTO();
            if (resp != null) {
                dto.setPromedio(resp.get("promedio") != null ? ((Number) resp.get("promedio")).doubleValue() : 0.0);
                dto.setTotal(resp.get("total")       != null ? ((Number) resp.get("total")).longValue()      : 0L);
            }
            return dto;
        } catch (Exception e) {
            ValoracionDTO dto = new ValoracionDTO(); dto.setPromedio(0.0); dto.setTotal(0L); return dto;
        }
    }

    public boolean valorar(Long recetaId, Integer puntuacion, String token) {
        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(
                    backendUrl + "/api/recetas/" + recetaId + "/valoraciones",
                    jsonEntity("{\"puntuacion\":" + puntuacion + "}", token), String.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception e) { return false; }
    }

    // Auth

    @SuppressWarnings("unchecked")
    public LoginResult login(String username, String password) {
        try {
            String body = "{\"username\":\"" + esc(username) + "\",\"password\":\"" + esc(password) + "\"}";
            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    backendUrl + "/api/auth/login", jsonEntity(body, null), Map.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                String token = (String) resp.getBody().get("token");
                String role  = (String) resp.getBody().getOrDefault("role", "ROLE_USER");
                return new LoginResult(token, role);
            }
        } catch (HttpClientErrorException.Unauthorized e) { /* credenciales incorrectas */ }
        catch (Exception e) { /* error de conexion */ }
        return null;
    }

    @SuppressWarnings("unchecked")
    public String registrar(String username, String nombre, String email, String password) {
        try {
            String body = String.format(
                "{\"username\":\"%s\",\"nombre\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
                esc(username), esc(q(nombre)), esc(email), esc(password));
            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    backendUrl + "/api/auth/register", jsonEntity(body, null), Map.class);
            return resp.getStatusCode().is2xxSuccessful() ? null : "Error al registrar.";
        } catch (HttpClientErrorException e) {
            try {
                Map<String, Object> err = e.getResponseBodyAs(Map.class);
                return err != null ? (String) err.get("error") : "Error al registrar el usuario.";
            } catch (Exception ignored) {}
            return "Error al registrar el usuario.";
        } catch (Exception e) { return "No se pudo conectar con el servidor."; }
    }

    // Favoritos

    public List<RecetaDTO> obtenerFavoritos(String token) {
        try {
            ResponseEntity<List<RecetaDTO>> resp = restTemplate.exchange(
                    backendUrl + "/api/favoritos", HttpMethod.GET, bearerEntity(token),
                    new ParameterizedTypeReference<List<RecetaDTO>>() {});
            return resp.getBody() != null ? resp.getBody() : List.of();
        } catch (Exception e) { return List.of(); }
    }

    public boolean agregarFavorito(Long recetaId, String token) {
        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(
                    backendUrl + "/api/favoritos/" + recetaId, bearerEntity(token), String.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception e) { return false; }
    }

    public boolean eliminarFavorito(Long recetaId, String token) {
        try {
            restTemplate.exchange(backendUrl + "/api/favoritos/" + recetaId,
                    HttpMethod.DELETE, bearerEntity(token), String.class);
            return true;
        } catch (Exception e) { return false; }
    }

    @SuppressWarnings("unchecked")
    public boolean esFavorito(Long recetaId, String token) {
        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                    backendUrl + "/api/favoritos/" + recetaId + "/es",
                    HttpMethod.GET, bearerEntity(token), Map.class);
            if (resp.getBody() != null) {
                Object val = resp.getBody().get("esFavorito");
                return val instanceof Boolean && (Boolean) val;
            }
            return false;
        } catch (Exception e) { return false; }
    }

    // Admin

    public List<Map<String, Object>> listarUsuariosAdmin(String token) {
        try {
            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                    backendUrl + "/api/admin/usuarios", HttpMethod.GET, bearerEntity(token),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            return resp.getBody() != null ? resp.getBody() : List.of();
        } catch (Exception e) { return List.of(); }
    }

    public List<Map<String, Object>> listarComentariosAdmin(String token) {
        try {
            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                    backendUrl + "/api/admin/comentarios", HttpMethod.GET, bearerEntity(token),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            return resp.getBody() != null ? resp.getBody() : List.of();
        } catch (Exception e) { return List.of(); }
    }

    public List<Map<String, Object>> listarRecetasAdmin(String token) {
        try {
            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                    backendUrl + "/api/admin/recetas", HttpMethod.GET, bearerEntity(token),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            return resp.getBody() != null ? resp.getBody() : List.of();
        } catch (Exception e) { return List.of(); }
    }

    public boolean eliminarComentarioAdmin(Long id, String token) {
        try {
            restTemplate.exchange(backendUrl + "/api/admin/comentarios/" + id,
                    HttpMethod.DELETE, bearerEntity(token), String.class);
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean eliminarUsuarioAdmin(Long id, String token) {
        try {
            restTemplate.exchange(backendUrl + "/api/admin/usuarios/" + id,
                    HttpMethod.DELETE, bearerEntity(token), String.class);
            return true;
        } catch (Exception e) { return false; }
    }

    @SuppressWarnings("unchecked")
    public boolean cambiarRolAdmin(Long id, String role, String token) {
        try {
            String body = "{\"role\":\"" + esc(role) + "\"}";
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_JSON);
            h.setBearerAuth(token);
            restTemplate.exchange(backendUrl + "/api/admin/usuarios/" + id + "/rol",
                    HttpMethod.PUT, new HttpEntity<>(body, h), Map.class);
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean eliminarRecetaAdmin(Long id, String token) {
        try {
            restTemplate.exchange(backendUrl + "/api/admin/recetas/" + id,
                    HttpMethod.DELETE, bearerEntity(token), String.class);
            return true;
        } catch (Exception e) { return false; }
    }

    // Perfil

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerPerfil(String token) {
        try {
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    backendUrl + "/api/usuario/perfil", HttpMethod.GET, bearerEntity(token),
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            return resp.getBody();
        } catch (Exception e) { return null; }
    }

    @SuppressWarnings("unchecked")
    public String actualizarPerfil(String nombre, String correo, String token) {
        try {
            String body = String.format("{\"nombre\":\"%s\",\"correo\":\"%s\"}", esc(q(nombre)), esc(q(correo)));
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_JSON);
            h.setBearerAuth(token);
            restTemplate.exchange(backendUrl + "/api/usuario/perfil",
                    HttpMethod.PUT, new HttpEntity<>(body, h), String.class);
            return null;
        } catch (HttpClientErrorException e) {
            try {
                Map<String, Object> err = e.getResponseBodyAs(Map.class);
                return err != null ? (String) err.get("error") : "Error al actualizar el perfil.";
            } catch (Exception ex) { return "Error al actualizar el perfil."; }
        } catch (Exception e) { return "Error de conexion con el servidor."; }
    }

    @SuppressWarnings("unchecked")
    public String cambiarPassword(String passwordActual, String passwordNueva, String token) {
        try {
            String body = String.format("{\"passwordActual\":\"%s\",\"passwordNueva\":\"%s\"}",
                    esc(passwordActual), esc(passwordNueva));
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_JSON);
            h.setBearerAuth(token);
            restTemplate.exchange(backendUrl + "/api/usuario/cambiar-password",
                    HttpMethod.PUT, new HttpEntity<>(body, h), String.class);
            return null;
        } catch (HttpClientErrorException e) {
            try {
                Map<String, Object> err = e.getResponseBodyAs(Map.class);
                return err != null ? (String) err.get("error") : "Error al cambiar la contrasena.";
            } catch (Exception ex) { return "Error al cambiar la contrasena."; }
        } catch (Exception e) { return "Error de conexion con el servidor."; }
    }

    // Helpers

    private HttpEntity<String> jsonEntity(String body, String token) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) h.setBearerAuth(token);
        return new HttpEntity<>(body, h);
    }

    private HttpEntity<Void> bearerEntity(String token) {
        HttpHeaders h = new HttpHeaders();
        if (token != null) h.setBearerAuth(token);
        return new HttpEntity<>(h);
    }

    private String q(String s) { return s != null ? s : ""; }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}