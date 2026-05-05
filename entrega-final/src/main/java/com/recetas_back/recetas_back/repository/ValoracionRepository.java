package com.recetas_back.recetas_back.repository;

import com.recetas_back.recetas_back.model.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {
    List<Valoracion> findByRecetaId(Long recetaId);

    @Query("SELECT AVG(v.puntuacion) FROM Valoracion v WHERE v.receta.id = :recetaId")
    Optional<Double> findAverageByRecetaId(@Param("recetaId") Long recetaId);

    @Query("SELECT COUNT(v) FROM Valoracion v WHERE v.receta.id = :recetaId")
    Long countByRecetaId(@Param("recetaId") Long recetaId);

    Optional<Valoracion> findByRecetaIdAndUsuarioId(Long recetaId, Long usuarioId);
}
