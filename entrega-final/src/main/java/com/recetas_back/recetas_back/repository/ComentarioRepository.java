package com.recetas_back.recetas_back.repository;

import com.recetas_back.recetas_back.model.Comentario;
import com.recetas_back.recetas_back.model.Comentario.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByRecetaIdAndEstadoOrderByCreatedAtDesc(Long recetaId, Estado estado);

    List<Comentario> findByRecetaIdOrderByCreatedAtDesc(Long recetaId);

    List<Comentario> findByEstadoOrderByCreatedAtAsc(Estado estado);
}
