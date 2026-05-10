package com.recetas_back.recetas_back.dto;

/**
 * DTO para la solicitud de registro de nuevos usuarios.
 * Recibe: username (login), nombre (display), email y password.
 */
public class RegisterRequest {

    private String username;
    private String nombre;
    private String email;
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}