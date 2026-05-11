package com.recetas_back.recetas_back.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Modelos - pruebas de getters y setters")
class ModelTest {

    @Test @DisplayName("Receta: todos los campos funcionan correctamente")
    void receta_gettersSetters() {
        Receta r = new Receta();
        r.setId(1L); r.setNombre("Sushi"); r.setTipoCocina("Asiatica");
        r.setPais("Japón"); r.setDificultad("Alta"); r.setTiempoPrepMinutos(90);
        r.setDescripcion("Fresco"); r.setInstrucciones("Paso 1");
        r.setIngredientes("arroz,salmón"); r.setImagenUrl("http://img.com/s.jpg");

        assertThat(r.getId()).isEqualTo(1L);
        assertThat(r.getNombre()).isEqualTo("Sushi");
        assertThat(r.getTipoCocina()).isEqualTo("Asiatica");
        assertThat(r.getPais()).isEqualTo("Japón");
        assertThat(r.getDificultad()).isEqualTo("Alta");
        assertThat(r.getTiempoPrepMinutos()).isEqualTo(90);
        assertThat(r.getDescripcion()).isEqualTo("Fresco");
        assertThat(r.getInstrucciones()).isEqualTo("Paso 1");
        assertThat(r.getIngredientes()).isEqualTo("arroz,salmón");
        assertThat(r.getImagenUrl()).isEqualTo("http://img.com/s.jpg");
        assertThat(r.getElementosMedia()).isNotNull();
        assertThat(r.getComentarios()).isNotNull();
        assertThat(r.getValoraciones()).isNotNull();
    }

    @Test @DisplayName("Receta: setters de listas funcionan")
    void receta_setListas() {
        Receta r = new Receta();
        r.setElementosMedia(new java.util.ArrayList<>());
        r.setComentarios(new java.util.ArrayList<>());
        r.setValoraciones(new java.util.ArrayList<>());
        assertThat(r.getElementosMedia()).isEmpty();
        assertThat(r.getComentarios()).isEmpty();
        assertThat(r.getValoraciones()).isEmpty();
    }

    @Test @DisplayName("Usuario: todos los campos funcionan correctamente")
    void usuario_gettersSetters() {
        Usuario u = new Usuario();
        u.setId(5L); u.setUsername("juan");
        u.setPassword("$2a$hash"); u.setRole("ROLE_USER");

        assertThat(u.getId()).isEqualTo(5L);
        assertThat(u.getUsername()).isEqualTo("juan");
        assertThat(u.getPassword()).isEqualTo("$2a$hash");
        assertThat(u.getRole()).isEqualTo("ROLE_USER");
    }

    @Test @DisplayName("Comentario: todos los campos funcionan correctamente")
    void comentario_gettersSetters() {
        Receta receta = new Receta(); receta.setId(1L);
        Usuario usuario = new Usuario(); usuario.setUsername("ana");
        LocalDateTime ahora = LocalDateTime.now();

        Comentario c = new Comentario();
        c.setId(10L); c.setContenido("Muy buena");
        c.setReceta(receta); c.setUsuario(usuario); c.setCreatedAt(ahora);

        assertThat(c.getId()).isEqualTo(10L);
        assertThat(c.getContenido()).isEqualTo("Muy buena");
        assertThat(c.getReceta().getId()).isEqualTo(1L);
        assertThat(c.getUsuario().getUsername()).isEqualTo("ana");
        assertThat(c.getCreatedAt()).isEqualTo(ahora);
    }

    @Test @DisplayName("Valoracion: todos los campos funcionan correctamente")
    void valoracion_gettersSetters() {
        Receta receta = new Receta(); receta.setId(2L);
        Usuario usuario = new Usuario(); usuario.setUsername("pedro");
        LocalDateTime ahora = LocalDateTime.now();

        Valoracion v = new Valoracion();
        v.setId(20L); v.setPuntuacion(5);
        v.setReceta(receta); v.setUsuario(usuario); v.setCreatedAt(ahora);

        assertThat(v.getId()).isEqualTo(20L);
        assertThat(v.getPuntuacion()).isEqualTo(5);
        assertThat(v.getReceta().getId()).isEqualTo(2L);
        assertThat(v.getUsuario().getUsername()).isEqualTo("pedro");
        assertThat(v.getCreatedAt()).isEqualTo(ahora);
    }

    @Test @DisplayName("Multimedia: todos los campos funcionan correctamente")
    void multimedia_gettersSetters() {
        Receta receta = new Receta(); receta.setId(3L);
        LocalDateTime ahora = LocalDateTime.now();

        Multimedia m = new Multimedia();
        m.setId(30L); m.setFileUrl("/uploads/foto.jpg");
        m.setFileType("FOTO"); m.setReceta(receta); m.setCreatedAt(ahora);

        assertThat(m.getId()).isEqualTo(30L);
        assertThat(m.getFileUrl()).isEqualTo("/uploads/foto.jpg");
        assertThat(m.getFileType()).isEqualTo("FOTO");
        assertThat(m.getReceta().getId()).isEqualTo(3L);
        assertThat(m.getCreatedAt()).isEqualTo(ahora);
    }

    @Test @DisplayName("Receta: createdAt funciona")
    void receta_createdAt() {
        Receta r = new Receta();
        LocalDateTime ahora = LocalDateTime.now();
        r.setCreatedAt(ahora);
        assertThat(r.getCreatedAt()).isEqualTo(ahora);
    }

    @Test @DisplayName("Comentario: estado y motivoRechazo funcionan")
    void comentario_moderacion() {
        Comentario c = new Comentario();
        c.setEstado(Comentario.Estado.PENDIENTE);
        c.setMotivoRechazo(null);
        assertThat(c.getEstado()).isEqualTo(Comentario.Estado.PENDIENTE);

        c.setEstado(Comentario.Estado.RECHAZADO);
        c.setMotivoRechazo("Spam");
        assertThat(c.getEstado()).isEqualTo(Comentario.Estado.RECHAZADO);
        assertThat(c.getMotivoRechazo()).isEqualTo("Spam");

        c.setEstado(Comentario.Estado.APROBADO);
        assertThat(c.getEstado()).isEqualTo(Comentario.Estado.APROBADO);
    }

    @Test @DisplayName("Banner: todos los campos funcionan correctamente")
    void banner_gettersSetters() {
        Banner b = new Banner();
        b.setId(100L); b.setTitulo("Promo");
        b.setEmpresa("Empresa SA"); b.setImagenUrl("https://img.com/x.jpg");
        b.setEnlaceUrl("https://link.com");
        b.setActivo(true); b.setOrden(5);
        LocalDateTime ahora = LocalDateTime.now();
        b.setCreatedAt(ahora);

        assertThat(b.getId()).isEqualTo(100L);
        assertThat(b.getTitulo()).isEqualTo("Promo");
        assertThat(b.getEmpresa()).isEqualTo("Empresa SA");
        assertThat(b.getImagenUrl()).isEqualTo("https://img.com/x.jpg");
        assertThat(b.getEnlaceUrl()).isEqualTo("https://link.com");
        assertThat(b.getActivo()).isTrue();
        assertThat(b.getOrden()).isEqualTo(5);
        assertThat(b.getCreatedAt()).isEqualTo(ahora);
    }
}
