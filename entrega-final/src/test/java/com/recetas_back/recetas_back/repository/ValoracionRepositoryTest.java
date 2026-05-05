package com.recetas_back.recetas_back.repository;

import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.model.Usuario;
import com.recetas_back.recetas_back.model.Valoracion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
@DisplayName("ValoracionRepository - pruebas con H2")
class ValoracionRepositoryTest {

    @Autowired private ValoracionRepository valoracionRepository;
    @Autowired private RecetaRepository recetaRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private Receta nuevaReceta() {
        Receta r = new Receta();
        r.setNombre("R");
        r.setTipoCocina("T");
        r.setPais("P");
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

    private Valoracion crear(Receta r, Usuario u, int p) {
        Valoracion v = new Valoracion();
        v.setReceta(r);
        v.setUsuario(u);
        v.setPuntuacion(p);
        return valoracionRepository.save(v);
    }

    @Test
    @DisplayName("save persiste valoración")
    void save_persiste() {
        Receta r = nuevaReceta();
        Usuario u = nuevoUsuario("u1");
        Valoracion v = crear(r, u, 5);
        assertThat(v.getId()).isNotNull();
    }

    @Test
    @DisplayName("findByRecetaId devuelve todas las valoraciones de la receta")
    void findByRecetaId() {
        Receta r = nuevaReceta();
        crear(r, nuevoUsuario("a"), 4);
        crear(r, nuevoUsuario("b"), 5);

        List<Valoracion> resultado = valoracionRepository.findByRecetaId(r.getId());
        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("findAverageByRecetaId calcula promedio")
    void findAverageByRecetaId() {
        Receta r = nuevaReceta();
        crear(r, nuevoUsuario("a"), 4);
        crear(r, nuevoUsuario("b"), 2);

        Optional<Double> avg = valoracionRepository.findAverageByRecetaId(r.getId());
        assertThat(avg).isPresent();
        assertThat(avg.get()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("countByRecetaId cuenta valoraciones")
    void countByRecetaId() {
        Receta r = nuevaReceta();
        crear(r, nuevoUsuario("a"), 4);
        crear(r, nuevoUsuario("b"), 5);
        crear(r, nuevoUsuario("c"), 3);

        Long count = valoracionRepository.countByRecetaId(r.getId());
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("findByRecetaIdAndUsuarioId encuentra valoración existente")
    void findByRecetaIdAndUsuarioId() {
        Receta r = nuevaReceta();
        Usuario u = nuevoUsuario("u1");
        crear(r, u, 5);

        Optional<Valoracion> encontrada = valoracionRepository.findByRecetaIdAndUsuarioId(r.getId(), u.getId());
        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getPuntuacion()).isEqualTo(5);
    }
}
