package com.recetas.frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Aplicación FRONTEND.
 * 
 * Excluye DataSourceAutoConfiguration porque el frontend NO tiene base de datos.
 * Toda la persistencia está en el backend.
 * 
 * Corre en el puerto 8081.
 * Consume la API REST del backend en el puerto 8080.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class FrontendApplication {
    public static void main(String[] args) {
        SpringApplication.run(FrontendApplication.class, args);
    }
}
