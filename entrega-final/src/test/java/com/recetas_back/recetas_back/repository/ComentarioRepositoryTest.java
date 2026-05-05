package com.recetas_back.recetas_back.repository;

import com.recetas_back.recetas_back.model.Comentario;
import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
@DisplayName("ComentarioRepository - pruebas con H2")
class ComentarioRepositoryTest {

    @Autowired private ComentarioRepository comentarioRepository;
    @Autowired private RecetaRepository recetaRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private Receta nuevaReceta() {
        Receta r = new Receta();
        r.setNombre("Receta x");
        r.setTipoCocina("X");
        r.setPais("X");
        r.setDificultad("Facil");
        r.setTiempoPrepMinutos(10);
        return recetaRepository.save(r);
    }

    private Usuario nuevoUsuario(String name) {
        Usuario u = new Usuario();
        u.setUsername(name);
        u.setPassword("p");
        u.setRole("ROLE_USER");
        return usuarioRepository.save(u);
    }

    @Test
    @DisplayName("save persiste comentario")
    void save_persisteComentario() {
        Receta r = nuevaReceta();
        Usuario u = nuevoUsuario("u1");

        Comentario c = new Comentario();
        c.setReceta(r);
        c.setUsuario(u);
        c.setContenido("Riquísimo");

        Comentario guardado = comentarioRepository.save(c);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getContenido()).isEqualTo("Riquísimo");
    }

    @Test
    @DisplayName("findByRecetaIdOrderByCreatedAtDesc devuelve comentarios de la receta")
    void findByRecetaId_devuelveComentarios() {
        Receta r = nuevaReceta();
        Usuario u = nuevoUsuario("u2");

        Comentario c1 = new Comentario();
        c1.setReceta(r); c1.setUsuario(u); c1.setContenido("Primero");
        comentarioRepository.save(c1);

        Comentario c2 = new Comentario();
        c2.setReceta(r); c2.setUsuario(u); c2.setContenido("Segundo");
        comentarioRepository.save(c2);

        List<Comentario> resultado = comentarioRepository.findByRecetaIdOrderByCreatedAtDesc(r.getId());
        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("findByRecetaIdOrderByCreatedAtDesc retorna lista vacía si no hay comentarios")
    void findByRecetaId_vacio() {
        Receta r = nuevaReceta();
        List<Comentario> resultado = comentarioRepository.findByRecetaIdOrderByCreatedAtDesc(r.getId());
        assertThat(resultado).isEmpty();
    }
}
