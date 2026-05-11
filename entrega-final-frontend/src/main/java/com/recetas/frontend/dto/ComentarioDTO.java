package com.recetas.frontend.dto;

public class ComentarioDTO {
    private Long id;
    private String contenido;
    private UsuarioDTO usuario;
    private String createdAt;
    private String estado;
    private String motivoRechazo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContenido() { return contenido; }
    public void setContenido(String c) { this.contenido = c; }
    public UsuarioDTO getUsuario() { return usuario; }
    public void setUsuario(UsuarioDTO u) { this.usuario = u; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String c) { this.createdAt = c; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
}
