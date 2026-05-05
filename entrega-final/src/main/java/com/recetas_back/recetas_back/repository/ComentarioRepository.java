package com.recetas_back.recetas_back.repository;

import com.recetas_back.recetas_back.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByRecetaIdOrderByCreatedAtDesc(Long recetaId);
}
