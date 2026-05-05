package com.recetas_back.recetas_back.controller;

import com.recetas_back.recetas_back.model.Receta;
import com.recetas_back.recetas_back.service.RecetaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("MediaController - pruebas unitarias")
class MediaControllerTest {

    private MediaController controller;
    private RecetaService recetaService;

    @BeforeEach
    void setUp() {
        recetaService = mock(RecetaService.class);
        controller = new MediaController();
        ReflectionTestUtils.setField(controller, "recetaService", recetaService);
    }

    @Test
    @DisplayName("Archivo vacío retorna 400")
    void uploadMedia_archivoVacio() {
        MockMultipartFile file = new MockMultipartFile("file", "x.png", "image/png", new byte[0]);
        ResponseEntity<?> resp = controller.uploadMedia(1L, file);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).isEqualTo("Archivo vacío");
    }

    @Test
    @DisplayName("MIME type no permitido retorna 400")
    void uploadMedia_mimeNoPermitido() {
        MockMultipartFile file = new MockMultipartFile("file", "x.exe", "application/octet-stream", "data".getBytes());
        ResponseEntity<?> resp = controller.uploadMedia(1L, file);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).isEqualTo("Tipo de archivo no permitido");
    }

    @Test
    @DisplayName("MIME null retorna 400")
    void uploadMedia_mimeNull() {
        MockMultipartFile file = new MockMultipartFile("file", "x.png", null, "data".getBytes());
        ResponseEntity<?> resp = controller.uploadMedia(1L, file);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).isEqualTo("Tipo de archivo no permitido");
    }

    @Test
    @DisplayName("Extensión no permitida retorna 400")
    void uploadMedia_extensionNoPermitida() {
        MockMultipartFile file = new MockMultipartFile("file", "x.bmp", "image/png", "data".getBytes());
        ResponseEntity<?> resp = controller.uploadMedia(1L, file);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).isEqualTo("Extensión no permitida");
    }

    @Test
    @DisplayName("Receta inexistente retorna 404")
    void uploadMedia_recetaNoExiste() {
        MockMultipartFile file = new MockMultipartFile("file", "x.png", "image/png", "data".getBytes());
        when(recetaService.obtenerPorId(99L)).thenReturn(Optional.empty());
        ResponseEntity<?> resp = controller.uploadMedia(99L, file);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Subida exitosa de imagen retorna 201")
    void uploadMedia_imagenExitosa() {
        Receta r = new Receta();
        r.setId(1L);
        when(recetaService.obtenerPorId(1L)).thenReturn(Optional.of(r));
        when(recetaService.guardar(any(Receta.class))).thenReturn(r);

        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", "imagen-bytes".getBytes());
        ResponseEntity<?> resp = controller.uploadMedia(1L, file);

        assertThat(resp.getStatusCode().value()).isEqualTo(201);
        verify(recetaService).guardar(any(Receta.class));
    }

    @Test
    @DisplayName("Subida exitosa de video retorna 201 y fileType VIDEO")
    void uploadMedia_videoExitoso() {
        Receta r = new Receta();
        r.setId(2L);
        when(recetaService.obtenerPorId(2L)).thenReturn(Optional.of(r));
        when(recetaService.guardar(any(Receta.class))).thenReturn(r);

        MockMultipartFile file = new MockMultipartFile("file", "clip.mp4", "video/mp4", "video-bytes".getBytes());
        ResponseEntity<?> resp = controller.uploadMedia(2L, file);

        assertThat(resp.getStatusCode().value()).isEqualTo(201);
        verify(recetaService).guardar(argThat(rec ->
            !rec.getElementosMedia().isEmpty()
                && "VIDEO".equals(rec.getElementosMedia().get(rec.getElementosMedia().size() - 1).getFileType())
        ));
    }

    @Test
    @DisplayName("Nombre de archivo vacío retorna 400")
    void uploadMedia_nombreVacio() {
        MockMultipartFile file = new MockMultipartFile("file", "", "image/png", "data".getBytes());
        ResponseEntity<?> resp = controller.uploadMedia(1L, file);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).isEqualTo("Nombre de archivo inválido");
    }
}
