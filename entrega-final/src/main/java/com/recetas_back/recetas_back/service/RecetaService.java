package com.recetas_back.recetas_back.service;

import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.model.Usuario;
import com.recetas_back.recetas_back.repository.RecetaRepository;
import com.recetas_back.recetas_back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Capa de Servicio para Receta.
 * Centraliza la lógica de negocio, separando responsabilidades del Controller.
 */
@Service
public class RecetaService {

    @Autowired
    private RecetaRepository recetaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

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

    /**
     * Crea una nueva receta publicada por el usuario autenticado.
     * Valida que los campos obligatorios no estén vacíos.
     *
     * @param nombre       nombre de la receta
     * @param tipoCocina   tipo de cocina (ej. Mexicana)
     * @param pais         país de origen
     * @param dificultad   Facil / Media / Alta
     * @param tiempoPrep   tiempo de preparación en minutos
     * @param descripcion  descripción breve
     * @param instrucciones pasos de preparación
     * @param ingredientes lista separada por comas
     * @param imagenUrl    URL de la imagen principal (puede ser null)
     * @param username     usuario que publica
     * @return Receta creada y persistida
     */
    public Receta crear(String nombre, String tipoCocina, String pais, String dificultad,
                        Integer tiempoPrep, String descripcion, String instrucciones,
                        String ingredientes, String imagenUrl, String username) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la receta es obligatorio.");
        }

        // Verificar que el usuario existe (por seguridad, aunque el JWT ya lo garantiza)
        usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));

        Receta receta = new Receta();
        receta.setNombre(nombre.trim());
        receta.setTipoCocina(tipoCocina);
        receta.setPais(pais);
        receta.setDificultad(dificultad);
        receta.setTiempoPrepMinutos(tiempoPrep != null ? tiempoPrep : 0);
        receta.setDescripcion(descripcion);
        receta.setInstrucciones(instrucciones);
        receta.setIngredientes(ingredientes);
        receta.setImagenUrl(imagenUrl != null && !imagenUrl.isBlank()
                ? imagenUrl
                : "https://images.unsplash.com/photo-1495521821757-a1efb6729352?w=600&q=80");

        return recetaRepository.save(receta);
    }

    /**
     * Elimina una receta por ID (solo ROLE_ADMIN).
     */
    public void eliminar(Long id) {
        recetaRepository.deleteById(id);
    }
}