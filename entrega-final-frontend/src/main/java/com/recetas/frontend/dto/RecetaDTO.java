package com.recetas.frontend.dto;

import java.util.List;
import java.util.ArrayList;

/**
 * DTO que representa la respuesta JSON del backend para una receta.
 * El frontend deserializa la respuesta del backend en este objeto.
 */
public class RecetaDTO {
    private Long id;
    private String nombre;
    private String tipoCocina;
    private String pais;
    private String dificultad;
    private Integer tiempoPrepMinutos;
    private String descripcion;
    private String instrucciones;
    private String ingredientes;
    private String imagenUrl;
    private String createdAt;
    private List<MultimediaDTO> elementosMedia = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipoCocina() { return tipoCocina; }
    public void setTipoCocina(String tipoCocina) { this.tipoCocina = tipoCocina; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getDificultad() { return dificultad; }
    public void setDificultad(String dificultad) { this.dificultad = dificultad; }
    public Integer getTiempoPrepMinutos() { return tiempoPrepMinutos; }
    public void setTiempoPrepMinutos(Integer t) { this.tiempoPrepMinutos = t; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getInstrucciones() { return instrucciones; }
    public void setInstrucciones(String instrucciones) { this.instrucciones = instrucciones; }
    public String getIngredientes() { return ingredientes; }
    public void setIngredientes(String ingredientes) { this.ingredientes = ingredientes; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public List<MultimediaDTO> getElementosMedia() { return elementosMedia; }
    public void setElementosMedia(List<MultimediaDTO> elementosMedia) { this.elementosMedia = elementosMedia; }
}
