package com.recetas_back.recetas_back.repository;

import com.recetas_back.recetas_back.model.Favorito;
import com.recetas_back.recetas_back.model.Receta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    /** Todos los favoritos de un usuario (para mostrar su lista). */
    List<Favorito> findByUsuarioId(Long usuarioId);

    /** Busca un favorito específico usuario-receta (para saber si ya existe). */
    Optional<Favorito> findByUsuarioIdAndRecetaId(Long usuarioId, Long recetaId);

    /** Proyección rápida de las recetas favoritas de un usuario. */
    List<Receta> findRecetaByUsuarioId(Long usuarioId);
}