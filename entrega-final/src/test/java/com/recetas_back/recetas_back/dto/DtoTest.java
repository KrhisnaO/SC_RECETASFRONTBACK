package com.recetas_back.recetas_back.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DTOs backend - getters y setters")
class DtoTest {

    @Test
    @DisplayName("LoginRequest: getters/setters funcionan")
    void loginRequest() {
        LoginRequest r = new LoginRequest();
        r.setUsername("admin");
        r.setPassword("secret");
        assertThat(r.getUsername()).isEqualTo("admin");
        assertThat(r.getPassword()).isEqualTo("secret");
    }

    @Test
    @DisplayName("LoginResponse: constructor y getters funcionan")
    void loginResponse() {
        LoginResponse r = new LoginResponse("eyJ.token", "admin");
        assertThat(r.getToken()).isEqualTo("eyJ.token");
        assertThat(r.getUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("ComentarioRequest: getters/setters funcionan")
    void comentarioRequest() {
        ComentarioRequest r = new ComentarioRequest();
        r.setContenido("Excelente receta");
        assertThat(r.getContenido()).isEqualTo("Excelente receta");
    }

    @Test
    @DisplayName("ValoracionRequest: getters/setters funcionan")
    void valoracionRequest() {
        ValoracionRequest r = new ValoracionRequest();
        r.setPuntuacion(5);
        assertThat(r.getPuntuacion()).isEqualTo(5);
    }
}
