package com.recetas_back.recetas_back.service;

import com.recetas_back.recetas_back.model.Comentario;
import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.model.Usuario;
import com.recetas_back.recetas_back.repository.ComentarioRepository;
import com.recetas_back.recetas_back.repository.RecetaRepository;
import com.recetas_back.recetas_back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private RecetaRepository recetaRepository;

    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public Usuario cambiarRol(Long usuarioId, String nuevoRol) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));
        usuario.setRole(nuevoRol);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminarUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new IllegalArgumentException("Usuario no encontrado: " + usuarioId);
        }
        usuarioRepository.deleteById(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<Comentario> listarTodosComentarios() {
        return comentarioRepository.findAll();
    }

    @Transactional
    public void eliminarComentario(Long comentarioId) {
        if (!comentarioRepository.existsById(comentarioId)) {
            throw new IllegalArgumentException("Comentario no encontrado: " + comentarioId);
        }
        comentarioRepository.deleteById(comentarioId);
    }

    @Transactional(readOnly = true)
    public List<Receta> listarTodasRecetas() {
        return recetaRepository.findAll();
    }

    @Transactional
    public void eliminarReceta(Long recetaId) {
        if (!recetaRepository.existsById(recetaId)) {
            throw new IllegalArgumentException("Receta no encontrada: " + recetaId);
        }
        recetaRepository.deleteById(recetaId);
    }
}