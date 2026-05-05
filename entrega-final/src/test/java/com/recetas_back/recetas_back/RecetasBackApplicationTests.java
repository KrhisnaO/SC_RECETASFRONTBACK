package com.recetas_back.recetas_back;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("RecetasBackApplication - carga del contexto Spring")
class RecetasBackApplicationTests {

    @Test
    @DisplayName("El contexto de Spring carga sin errores")
    void contextLoads() {
        // Verifica que todos los beans se inicializan correctamente
    }
}
