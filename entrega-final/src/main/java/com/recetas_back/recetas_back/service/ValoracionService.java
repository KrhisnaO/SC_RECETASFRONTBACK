package com.recetas_back.recetas_back.service;

import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.model.Usuario;
import com.recetas_back.recetas_back.model.Valoracion;
import com.recetas_back.recetas_back.repository.RecetaRepository;
import com.recetas_back.recetas_back.repository.UsuarioRepository;
import com.recetas_back.recetas_back.repository.ValoracionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ValoracionService {

    @Autowired
    private ValoracionRepository valoracionRepository;

    @Autowired
    private RecetaRepository recetaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public double obtenerPromedio(Long recetaId) {
        return valoracionRepository.findAverageByRecetaId(recetaId).orElse(0.0);
    }

    public long obtenerTotal(Long recetaId) {
        return valoracionRepository.countByRecetaId(recetaId);
    }

    public Valoracion guardarOActualizar(Long recetaId, String username, Integer puntuacion) {
        Receta receta = recetaRepository.findById(recetaId)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada: " + recetaId));

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));

        Optional<Valoracion> existente = valoracionRepository.findByRecetaIdAndUsuarioId(recetaId, usuario.getId());
        Valoracion valoracion;
        if (existente.isPresent()) {
            valoracion = existente.get();
            valoracion.setPuntuacion(puntuacion);
        } else {
            valoracion = new Valoracion();
            valoracion.setReceta(receta);
            valoracion.setUsuario(usuario);
            valoracion.setPuntuacion(puntuacion);
        }
        return valoracionRepository.save(valoracion);
    }
}
