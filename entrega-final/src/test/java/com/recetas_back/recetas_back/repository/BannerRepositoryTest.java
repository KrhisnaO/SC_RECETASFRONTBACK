package com.recetas_back.recetas_back.repository;

import com.recetas_back.recetas_back.model.Banner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
@DisplayName("BannerRepository - pruebas con H2")
class BannerRepositoryTest {

    @Autowired private BannerRepository bannerRepository;

    private Banner crear(String titulo, boolean activo, int orden) {
        Banner b = new Banner();
        b.setTitulo(titulo);
        b.setImagenUrl("https://img.com/" + titulo + ".jpg");
        b.setActivo(activo);
        b.setOrden(orden);
        return bannerRepository.save(b);
    }

    @Test
    @DisplayName("findByActivoTrueOrderByOrdenAscIdAsc retorna solo activos en orden")
    void findActivos_devuelveOrdenado() {
        crear("Inactivo", false, 0);
        crear("Banner B", true, 2);
        crear("Banner A", true, 1);

        List<Banner> activos = bannerRepository.findByActivoTrueOrderByOrdenAscIdAsc();

        assertThat(activos).hasSize(2);
        assertThat(activos.get(0).getTitulo()).isEqualTo("Banner A");
        assertThat(activos.get(1).getTitulo()).isEqualTo("Banner B");
    }

    @Test
    @DisplayName("findByActivoTrueOrderByOrdenAscIdAsc retorna vacío si todos inactivos")
    void findActivos_todosInactivos_vacio() {
        crear("X", false, 0);
        crear("Y", false, 0);
        assertThat(bannerRepository.findByActivoTrueOrderByOrdenAscIdAsc()).isEmpty();
    }

    @Test
    @DisplayName("save asigna ID y createdAt")
    void save_persisteConCreatedAt() {
        Banner b = crear("Nuevo", true, 0);
        assertThat(b.getId()).isNotNull();
        assertThat(b.getCreatedAt()).isNotNull();
    }
}
