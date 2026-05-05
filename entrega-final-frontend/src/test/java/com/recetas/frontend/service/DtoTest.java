package com.recetas.frontend.service;

import com.recetas.frontend.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DTOs Frontend - pruebas de getters y setters")
class DtoTest {

    @Test
    @DisplayName("RecetaDTO: todos los campos funcionan")
    void recetaDTO_camposFuncionan() {
        RecetaDTO r = new RecetaDTO();
        r.setId(1L); r.setNombre("Sushi"); r.setTipoCocina("Asiatica");
        r.setPais("Japón"); r.setDificultad("Alta"); r.setTiempoPrepMinutos(90);
        r.setDescripcion("Desc"); r.setInstrucciones("Pasos"); r.setIngredientes("arroz");
        r.setImagenUrl("http://img.com/s.jpg");

        assertThat(r.getId()).isEqualTo(1L);
        assertThat(r.getNombre()).isEqualTo("Sushi");
        assertThat(r.getTipoCocina()).isEqualTo("Asiatica");
        assertThat(r.getPais()).isEqualTo("Japón");
        assertThat(r.getDificultad()).isEqualTo("Alta");
        assertThat(r.getTiempoPrepMinutos()).isEqualTo(90);
        assertThat(r.getDescripcion()).isEqualTo("Desc");
        assertThat(r.getInstrucciones()).isEqualTo("Pasos");
        assertThat(r.getIngredientes()).isEqualTo("arroz");
        assertThat(r.getImagenUrl()).isEqualTo("http://img.com/s.jpg");
        assertThat(r.getElementosMedia()).isNotNull();
    }

    @Test
    @DisplayName("RecetaDTO: setElementosMedia funciona")
    void recetaDTO_setElementosMedia() {
        RecetaDTO r = new RecetaDTO();
        MultimediaDTO m = new MultimediaDTO();
        m.setId(1L); m.setFileUrl("/foto.jpg"); m.setFileType("FOTO");
        r.setElementosMedia(List.of(m));
        assertThat(r.getElementosMedia()).hasSize(1);
        assertThat(r.getElementosMedia().get(0).getFileType()).isEqualTo("FOTO");
    }

    @Test
    @DisplayName("ComentarioDTO: todos los campos funcionan")
    void comentarioDTO_camposFuncionan() {
        ComentarioDTO c = new ComentarioDTO();
        c.setId(10L); c.setContenido("Muy buena"); c.setCreatedAt("2025-01-01");
        UsuarioDTO u = new UsuarioDTO();
        u.setId(1L); u.setUsername("carlos");
        c.setUsuario(u);

        assertThat(c.getId()).isEqualTo(10L);
        assertThat(c.getContenido()).isEqualTo("Muy buena");
        assertThat(c.getCreatedAt()).isEqualTo("2025-01-01");
        assertThat(c.getUsuario().getUsername()).isEqualTo("carlos");
        assertThat(c.getUsuario().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("ValoracionDTO: promedio y total funcionan")
    void valoracionDTO_camposFuncionan() {
        ValoracionDTO v = new ValoracionDTO();
        v.setPromedio(4.7); v.setTotal(15L);
        assertThat(v.getPromedio()).isEqualTo(4.7);
        assertThat(v.getTotal()).isEqualTo(15L);
    }

    @Test
    @DisplayName("MultimediaDTO: todos los campos funcionan")
    void multimediaDTO_camposFuncionan() {
        MultimediaDTO m = new MultimediaDTO();
        m.setId(5L); m.setFileUrl("/vid.mp4"); m.setFileType("VIDEO");
        assertThat(m.getId()).isEqualTo(5L);
        assertThat(m.getFileUrl()).isEqualTo("/vid.mp4");
        assertThat(m.getFileType()).isEqualTo("VIDEO");
    }
}
