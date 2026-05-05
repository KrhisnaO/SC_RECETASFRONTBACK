package com.recetas_back.recetas_back.repository;

import com.recetas_back.recetas_back.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
@DisplayName("UsuarioRepository - pruebas con H2")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("save persiste un usuario con id generado")
    void save_persisteUsuario() {
        Usuario u = new Usuario();
        u.setUsername("test1");
        u.setPassword("pass");
        u.setRole("ROLE_USER");

        Usuario guardado = usuarioRepository.save(u);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getUsername()).isEqualTo("test1");
    }

    @Test
    @DisplayName("findByUsername encuentra usuario existente")
    void findByUsername_existente() {
        Usuario u = new Usuario();
        u.setUsername("alice");
        u.setPassword("pass");
        u.setRole("ROLE_USER");
        usuarioRepository.save(u);

        Optional<Usuario> encontrado = usuarioRepository.findByUsername("alice");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getUsername()).isEqualTo("alice");
    }

    @Test
    @DisplayName("findByUsername retorna empty si no existe")
    void findByUsername_inexistente() {
        Optional<Usuario> encontrado = usuarioRepository.findByUsername("ghost");
        assertThat(encontrado).isEmpty();
    }
}
