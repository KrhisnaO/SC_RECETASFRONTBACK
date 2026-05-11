package com.recetas_back.recetas_back.service;

import com.recetas_back.recetas_back.exception.ContenidoNoPermitidoException;
import com.recetas_back.recetas_back.model.Comentario;
import com.recetas_back.recetas_back.model.Comentario.Estado;
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

    @Autowired
    private ModeracionService moderacionService;

    public List<Comentario> listarPorReceta(Long recetaId) {
        return comentarioRepository.findByRecetaIdAndEstadoOrderByCreatedAtDesc(recetaId, Estado.APROBADO);
    }

    public List<Comentario> listarTodosPorReceta(Long recetaId) {
        return comentarioRepository.findByRecetaIdOrderByCreatedAtDesc(recetaId);
    }

    public List<Comentario> listarPorEstado(Estado estado) {
        return comentarioRepository.findByEstadoOrderByCreatedAtAsc(estado);
    }

    public Comentario agregar(Long recetaId, String username, String contenido) {
        Receta receta = recetaRepository.findById(recetaId)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada: " + recetaId));

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));

        String motivo = moderacionService.motivoRechazoSiHay(contenido);

        Comentario comentario = new Comentario();
        comentario.setReceta(receta);
        comentario.setUsuario(usuario);
        comentario.setContenido(contenido);

        if (motivo != null) {
            comentario.setEstado(Estado.RECHAZADO);
            comentario.setMotivoRechazo(motivo);
            comentarioRepository.save(comentario);
            throw new ContenidoNoPermitidoException(motivo);
        }

        comentario.setEstado(Estado.PENDIENTE);
        return comentarioRepository.save(comentario);
    }

    public Comentario aprobar(Long comentarioId) {
        Comentario c = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new IllegalArgumentException("Comentario no encontrado: " + comentarioId));
        c.setEstado(Estado.APROBADO);
        c.setMotivoRechazo(null);
        return comentarioRepository.save(c);
    }

    public Comentario rechazar(Long comentarioId, String motivo) {
        Comentario c = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new IllegalArgumentException("Comentario no encontrado: " + comentarioId));
        c.setEstado(Estado.RECHAZADO);
        c.setMotivoRechazo(motivo != null && !motivo.isBlank() ? motivo : "Rechazado por moderador.");
        return comentarioRepository.save(c);
    }
}
