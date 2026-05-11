package com.recetas_back.recetas_back.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ModeracionService - filtro de contenido")
class ModeracionServiceTest {

    private final ModeracionService moderacionService = new ModeracionService();

    @Test @DisplayName("acepta un comentario neutro")
    void aceptaContenidoNeutro() {
        assertThat(moderacionService.motivoRechazoSiHay(
                "Excelente receta, la voy a probar este fin de semana."))
                .isNull();
    }

    @Test @DisplayName("rechaza contenido vacío")
    void rechazaContenidoVacio() {
        assertThat(moderacionService.motivoRechazoSiHay("")).contains("vacío");
        assertThat(moderacionService.motivoRechazoSiHay("   ")).contains("vacío");
        assertThat(moderacionService.motivoRechazoSiHay(null)).contains("vacío");
    }

    @Test @DisplayName("rechaza insultos directos")
    void rechazaInsultos() {
        assertThat(moderacionService.motivoRechazoSiHay("Esto es una mierda"))
                .contains("prohibida");
        assertThat(moderacionService.motivoRechazoSiHay("Eres un idiota"))
                .contains("prohibida");
    }

    @Test @DisplayName("rechaza insultos con tildes (normalización)")
    void rechazaInsultosNormalizados() {
        assertThat(moderacionService.motivoRechazoSiHay("Eres un imbécil"))
                .contains("prohibida");
    }

    @Test @DisplayName("rechaza spam/contenido comercial inapropiado")
    void rechazaSpam() {
        assertThat(moderacionService.motivoRechazoSiHay("Compra viagra aquí"))
                .contains("prohibida");
        assertThat(moderacionService.motivoRechazoSiHay("Mejor casino online"))
                .contains("prohibida");
    }

    @Test @DisplayName("evita falsos positivos por subcadena")
    void evitaFalsosPositivos() {
        assertThat(moderacionService.motivoRechazoSiHay(
                "Tomé una clase de cocina y aprendí mucho")).isNull();
    }

    @Test @DisplayName("rechaza comentario excesivamente largo")
    void rechazaComentarioLargo() {
        String texto = "a".repeat(1001);
        assertThat(moderacionService.motivoRechazoSiHay(texto)).contains("largo");
    }

    @Test @DisplayName("acepta comentario en el límite de tamaño")
    void aceptaComentarioEnLimite() {
        String texto = "a".repeat(1000);
        assertThat(moderacionService.motivoRechazoSiHay(texto)).isNull();
    }
}
