package com.recetas_back.recetas_back.repository;

import com.recetas_back.recetas_back.model.Receta;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RecetaRepository extends JpaRepository<Receta, Long> {

    @Query("SELECT r FROM Receta r WHERE " +
            "(:query IS NULL OR :query = '' OR LOWER(r.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(r.ingredientes) LIKE LOWER(CONCAT('%', :query, '%'))) "
            +
            "AND (:tipoCocina IS NULL OR :tipoCocina = '' OR r.tipoCocina = :tipoCocina) " +
            "AND (:pais IS NULL OR :pais = '' OR r.pais = :pais) " +
            "AND (:dificultad IS NULL OR :dificultad = '' OR r.dificultad = :dificultad)")
    List<Receta> searchRecetas(@Param("query") String query,
            @Param("tipoCocina") String tipoCocina,
            @Param("pais") String pais,
            @Param("dificultad") String dificultad);

    @Query("SELECT r FROM Receta r ORDER BY COALESCE(r.createdAt, CAST('1970-01-01T00:00:00' AS timestamp)) DESC, r.id DESC")
    List<Receta> findRecientes(Pageable pageable);

    @Query("SELECT r FROM Receta r " +
            "LEFT JOIN r.valoraciones v " +
            "GROUP BY r.id " +
            "ORDER BY (COALESCE(AVG(v.puntuacion), 0) * COUNT(v) + " +
            "(SELECT COUNT(f) FROM Favorito f WHERE f.receta = r)) DESC, r.id DESC")
    List<Receta> findPopulares(Pageable pageable);
}
