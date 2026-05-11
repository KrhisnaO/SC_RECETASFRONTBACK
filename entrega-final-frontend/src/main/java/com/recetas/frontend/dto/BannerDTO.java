package com.recetas.frontend.dto;

public class BannerDTO {
    private Long id;
    private String titulo;
    private String empresa;
    private String imagenUrl;
    private String enlaceUrl;
    private Boolean activo;
    private Integer orden;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public String getEnlaceUrl() { return enlaceUrl; }
    public void setEnlaceUrl(String enlaceUrl) { this.enlaceUrl = enlaceUrl; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}
