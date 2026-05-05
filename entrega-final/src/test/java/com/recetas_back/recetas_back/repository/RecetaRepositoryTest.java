package com.recetas_back.recetas_back.repository;

import com.recetas_back.recetas_back.model.Receta;
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
@DisplayName("RecetaRepository - pruebas con H2")
class RecetaRepositoryTest {

    @Autowired
    private RecetaRepository recetaRepository;

    private Receta crearReceta(String nombre, String tipo, String pais, String dificultad, String ingredientes) {
        Receta r = new Receta();
        r.setNombre(nombre);
        r.setTipoCocina(tipo);
        r.setPais(pais);
        r.setDificultad(dificultad);
        r.setTiempoPrepMinutos(30);
        r.setIngredientes(ingredientes);
        return recetaRepository.save(r);
    }

    @Test
    @DisplayName("save y findById funcionan")
    void saveYFindById() {
        Receta r = crearReceta("Tacos", "Mexicana", "Mexico", "Facil", "tortilla,carne");
        Optional<Receta> encontrada = recetaRepository.findById(r.getId());
        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getNombre()).isEqualTo("Tacos");
    }

    @Test
    @DisplayName("searchRecetas filtra por query en nombre")
    void searchRecetas_porQuery() {
        crearReceta("Sushi", "Asiatica", "Japon", "Alta", "arroz,salmon");
        crearReceta("Pizza", "Italiana", "Italia", "Media", "harina,queso");

        List<Receta> resultado = recetaRepository.searchRecetas("sushi", null, null, null);
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Sushi");
    }

    @Test
    @DisplayName("searchRecetas filtra por tipoCocina")
    void searchRecetas_porTipoCocina() {
        crearReceta("Sushi", "Asiatica", "Japon", "Alta", "arroz");
        crearReceta("Pizza", "Italiana", "Italia", "Media", "harina");

        List<Receta> resultado = recetaRepository.searchRecetas("", "Asiatica", "", "");
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipoCocina()).isEqualTo("Asiatica");
    }

    @Test
    @DisplayName("searchRecetas con todos los filtros nulos retorna todas")
    void searchRecetas_sinFiltros() {
        crearReceta("R1", "T", "P", "D", "i1");
        crearReceta("R2", "T", "P", "D", "i2");

        List<Receta> resultado = recetaRepository.searchRecetas(null, null, null, null);
        assertThat(resultado).hasSizeGreaterThanOrEqualTo(2);
    }
}
