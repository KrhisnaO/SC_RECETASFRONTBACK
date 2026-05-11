package com.recetas_back.recetas_back.service;

import com.recetas_back.recetas_back.model.Favorito;
import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.model.Usuario;
import com.recetas_back.recetas_back.repository.FavoritoRepository;
import com.recetas_back.recetas_back.repository.RecetaRepository;
import com.recetas_back.recetas_back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Lógica de negocio para favoritos.
 * Permite agregar, eliminar y listar recetas favoritas de un usuario.
 */
@Service
public class FavoritoService {

    private static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado";

    @Autowired
    private FavoritoRepository favoritoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RecetaRepository recetaRepository;

    /**
     * Agrega una receta a los favoritos del usuario.
     * Si ya existe, no hace nada (idempotente).
     */
    public Favorito agregar(String username, Long recetaId) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USUARIO_NO_ENCONTRADO));
        Receta receta = recetaRepository.findById(recetaId)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada"));

        Optional<Favorito> existente = favoritoRepository
                .findByUsuarioIdAndRecetaId(usuario.getId(), recetaId);
        if (existente.isPresent()) {
            return existente.get(); // ya era favorito
        }

        Favorito favorito = new Favorito();
        favorito.setUsuario(usuario);
        favorito.setReceta(receta);
        return favoritoRepository.save(favorito);
    }

    /**
     * Elimina una receta de los favoritos del usuario.
     * Si no existía, no hace nada.
     */
    public void eliminar(String username, Long recetaId) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USUARIO_NO_ENCONTRADO));

        favoritoRepository.findByUsuarioIdAndRecetaId(usuario.getId(), recetaId)
                .ifPresent(favoritoRepository::delete);
    }

    /**
     * Lista todas las recetas favoritas de un usuario.
     */
    public List<Receta> listar(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USUARIO_NO_ENCONTRADO));

        return favoritoRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(Favorito::getReceta)
                .toList();
    }

    /**
     * Indica si una receta ya es favorita del usuario.
     */
    public boolean esFavorito(String username, Long recetaId) {
        return usuarioRepository.findByUsername(username)
                .map(u -> favoritoRepository.findByUsuarioIdAndRecetaId(u.getId(), recetaId).isPresent())
                .orElse(false);
    }
}