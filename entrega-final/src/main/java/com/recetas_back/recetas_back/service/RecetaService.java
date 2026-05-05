package com.recetas_back.recetas_back.service;

import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.repository.RecetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Capa de Servicio para Receta.
 * Centraliza la lógica de negocio, separando responsabilidades del Controller.
 * El Controller sólo orquesta: recibe petición → llama al Service → devuelve vista.
 */
@Service
public class RecetaService {

    @Autowired
    private RecetaRepository recetaRepository;

    public List<Receta> listarTodas() {
        return recetaRepository.findAll();
    }

    public Optional<Receta> obtenerPorId(Long id) {
        return recetaRepository.findById(id);
    }

    public List<Receta> buscar(String query, String tipoCocina, String pais, String dificultad) {
        return recetaRepository.searchRecetas(query, tipoCocina, pais, dificultad);
    }

    public Receta guardar(Receta receta) {
        return recetaRepository.save(receta);
    }
}
