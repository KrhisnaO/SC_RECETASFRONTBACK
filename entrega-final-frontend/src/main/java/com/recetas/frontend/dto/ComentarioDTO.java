package com.recetas.frontend.dto;

public class ComentarioDTO {
    private Long id;
    private String contenido;
    private UsuarioDTO usuario;
    private String createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContenido() { return contenido; }
    public void setContenido(String c) { this.contenido = c; }
    public UsuarioDTO getUsuario() { return usuario; }
    public void setUsuario(UsuarioDTO u) { this.usuario = u; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String c) { this.createdAt = c; }
}
