package com.recetas_back.recetas_back.service;

import com.recetas_back.recetas_back.model.Comentario;
import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.model.Usuario;
import com.recetas_back.recetas_back.repository.ComentarioRepository;
import com.recetas_back.recetas_back.repository.RecetaRepository;
import com.recetas_back.recetas_back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComentarioService {

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private RecetaRepository recetaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Comentario> listarPorReceta(Long recetaId) {
        return comentarioRepository.findByRecetaIdOrderByCreatedAtDesc(recetaId);
    }

    /**
     * Agrega un comentario. Lanza IllegalArgumentException si receta o usuario no existen.
     */
    public Comentario agregar(Long recetaId, String username, String contenido) {
        Receta receta = recetaRepository.findById(recetaId)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada: " + recetaId));

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));

        Comentario comentario = new Comentario();
        comentario.setReceta(receta);
        comentario.setUsuario(usuario);
        comentario.setContenido(contenido);
        return comentarioRepository.save(comentario);
    }
}
