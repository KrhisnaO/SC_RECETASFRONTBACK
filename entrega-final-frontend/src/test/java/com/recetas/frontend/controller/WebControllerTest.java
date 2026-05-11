package com.recetas.frontend.controller;

import com.recetas.frontend.dto.*;
import com.recetas.frontend.service.ApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebController.class)
@DisplayName("WebController Frontend - pruebas unitarias con MockMvc")
class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApiClient apiClient;

    private RecetaDTO receta;

    @BeforeEach
    void setUp() {
        receta = new RecetaDTO();
        receta.setId(1L);
        receta.setNombre("Sushi");
        receta.setTipoCocina("Asiatica");
        receta.setDificultad("Alta");
        receta.setTiempoPrepMinutos(90);
        receta.setDescripcion("Sushi fresco");
        receta.setIngredientes("arroz,salmón");
    }

    // ── GET /home ────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /home retorna vista home con banners, recientes y populares")
    void home_retornaVistaHome() throws Exception {
        when(apiClient.obtenerBanners()).thenReturn(List.of());
        when(apiClient.obtenerRecientes(anyInt())).thenReturn(List.of(receta));
        when(apiClient.obtenerPopulares(anyInt())).thenReturn(List.of(receta));
        when(apiClient.obtenerRecetas()).thenReturn(List.of(receta));
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("banners", "recientes", "populares", "recetas"));
    }

    @Test
    @DisplayName("GET / redirige a home")
    void raiz_retornaHome() throws Exception {
        when(apiClient.obtenerBanners()).thenReturn(List.of());
        when(apiClient.obtenerRecientes(anyInt())).thenReturn(List.of());
        when(apiClient.obtenerPopulares(anyInt())).thenReturn(List.of());
        when(apiClient.obtenerRecetas()).thenReturn(List.of());
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Test
    @DisplayName("GET /recetas también carga home")
    void recetas_retornaHome() throws Exception {
        when(apiClient.obtenerBanners()).thenReturn(List.of());
        when(apiClient.obtenerRecientes(anyInt())).thenReturn(List.of());
        when(apiClient.obtenerPopulares(anyInt())).thenReturn(List.of());
        when(apiClient.obtenerRecetas()).thenReturn(List.of());
        mockMvc.perform(get("/recetas"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    // ── GET /buscar ──────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /buscar retorna vista buscar con resultados")
    void buscar_retornaVistaBuscar() throws Exception {
        when(apiClient.buscarRecetas(null, null, null, null)).thenReturn(List.of(receta));
        mockMvc.perform(get("/buscar"))
                .andExpect(status().isOk())
                .andExpect(view().name("buscar"))
                .andExpect(model().attributeExists("recetas"));
    }

    @Test
    @DisplayName("GET /buscar con parámetros pasa filtros al ApiClient")
    void buscar_conParametros() throws Exception {
        when(apiClient.buscarRecetas("sushi", "Asiatica", null, null)).thenReturn(List.of(receta));
        mockMvc.perform(get("/buscar").param("query","sushi").param("tipoCocina","Asiatica"))
                .andExpect(status().isOk())
                .andExpect(view().name("buscar"));
    }

    // ── GET /receta/{id} ─────────────────────────────────────────────────
    @Test
    @DisplayName("GET /receta/{id} retorna detalle cuando receta existe")
    void detalle_retornaVistaDetalle() throws Exception {
        ValoracionDTO val = new ValoracionDTO();
        val.setPromedio(4.5); val.setTotal(5L);

        when(apiClient.obtenerReceta(1L)).thenReturn(receta);
        when(apiClient.obtenerComentarios(1L)).thenReturn(List.of());
        when(apiClient.obtenerValoracion(1L)).thenReturn(val);

        mockMvc.perform(get("/receta/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("detalle"))
                .andExpect(model().attributeExists("receta", "comentarios", "promedioValoracion", "totalValoraciones"));
    }

    @Test
    @DisplayName("GET /receta/{id} redirige a home si receta no existe")
    void detalle_redirigeSiNoExiste() throws Exception {
        when(apiClient.obtenerReceta(99L)).thenReturn(null);
        mockMvc.perform(get("/receta/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    // ── GET /login ───────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /login retorna vista login")
    void loginForm_retornaVistaLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    // ── POST /login ──────────────────────────────────────────────────────
    @Test
    @DisplayName("POST /login redirige a home si LoginResult válido")
    void login_redirigeSiToken() throws Exception {
        when(apiClient.login("admin", "password"))
                .thenReturn(new ApiClient.LoginResult("eyJ.token", "ROLE_USER"));
        mockMvc.perform(post("/login")
                        .param("username","admin")
                        .param("password","password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    @DisplayName("POST /login marca IS_ADMIN si role es ROLE_ADMIN")
    void login_marcaAdminSiRolEsAdmin() throws Exception {
        when(apiClient.login("admin", "password"))
                .thenReturn(new ApiClient.LoginResult("eyJ.token", "ROLE_ADMIN"));
        mockMvc.perform(post("/login")
                        .param("username","admin")
                        .param("password","password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    @DisplayName("POST /login vuelve a login si credenciales inválidas")
    void login_vuelveALoginSiError() throws Exception {
        when(apiClient.login("admin", "wrong")).thenReturn(null);
        mockMvc.perform(post("/login")
                        .param("username","admin")
                        .param("password","wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"));
    }

    // ── GET /logout ──────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /logout invalida sesión y redirige a login")
    void logout_redirigeSiLogout() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }

    // ── POST /receta/{id}/comentar ────────────────────────────────────────
    @Test
    @DisplayName("POST /receta/{id}/comentar con sesión envía y redirige al detalle")
    void comentar_conSesionRedirige() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "eyJ.token");

        when(apiClient.publicarComentarioDetallado(eq(1L), anyString(), eq("eyJ.token")))
                .thenReturn(new ApiClient.ComentarioResult("OK", null));

        mockMvc.perform(post("/receta/1/comentar")
                        .param("contenido","Muy rica!")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receta/1#comentarios"));
    }

    @Test
    @DisplayName("POST /receta/{id}/comentar redirige a login si no hay sesión")
    void comentar_sinSesionRedirige() throws Exception {
        mockMvc.perform(post("/receta/1/comentar").param("contenido","Texto"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verify(apiClient, never()).publicarComentarioDetallado(any(), any(), any());
    }

    @Test
    @DisplayName("POST /receta/{id}/comentar muestra mensaje si moderación rechaza")
    void comentar_rechazadoPorModeracion() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "eyJ.token");

        when(apiClient.publicarComentarioDetallado(eq(1L), anyString(), eq("eyJ.token")))
                .thenReturn(new ApiClient.ComentarioResult("REJECTED", "Lenguaje no permitido"));

        mockMvc.perform(post("/receta/1/comentar")
                        .param("contenido","contenido prohibido")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receta/1#comentarios"))
                .andExpect(flash().attribute("comentarioEstado", "REJECTED"));
    }

    // ── POST /receta/{id}/valorar ─────────────────────────────────────────
    @Test
    @DisplayName("POST /receta/{id}/valorar redirige al detalle con sesión activa")
    void valorar_conSesionRedirige() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "eyJ.token");

        when(apiClient.valorar(eq(1L), eq(5), eq("eyJ.token"))).thenReturn(true);

        mockMvc.perform(post("/receta/1/valorar")
                        .param("puntuacion","5")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receta/1"));
    }

    @Test
    @DisplayName("POST /receta/{id}/valorar redirige sin llamar al API si no hay sesión")
    void valorar_sinSesionRedirige() throws Exception {
        mockMvc.perform(post("/receta/1/valorar").param("puntuacion","4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receta/1"));
        verify(apiClient, never()).valorar(any(), any(), any());
    }

    // ── GET /admin con filtro de moderación ──────────────────────────────
    @Test
    @DisplayName("GET /admin pasa todos los atributos a la vista admin")
    void admin_retornaVistaConAtributos() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "eyJ.token");
        session.setAttribute("IS_ADMIN", Boolean.TRUE);

        when(apiClient.listarUsuariosAdmin(anyString())).thenReturn(List.of());
        when(apiClient.listarComentariosPorEstado(anyString(), anyString())).thenReturn(List.of());
        when(apiClient.listarRecetasAdmin(anyString())).thenReturn(List.of());
        when(apiClient.listarBannersAdmin(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attributeExists("usuarios", "comentarios", "recetas",
                        "banners", "estadoFiltro", "pendientesCount"));
    }

    @Test
    @DisplayName("GET /admin redirige a home si no es admin")
    void admin_redirigeSiNoEsAdmin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "eyJ.token");
        session.setAttribute("IS_ADMIN", Boolean.FALSE);

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    @DisplayName("POST /admin/comentarios/{id}/aprobar redirige a moderación")
    void aprobarComentario_redirige() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "eyJ.token");
        session.setAttribute("IS_ADMIN", Boolean.TRUE);

        when(apiClient.aprobarComentarioAdmin(eq(5L), anyString())).thenReturn(true);

        mockMvc.perform(post("/admin/comentarios/5/aprobar").session(session))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("POST /admin/banners/crear delega en ApiClient")
    void crearBanner_delegaApi() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "eyJ.token");
        session.setAttribute("IS_ADMIN", Boolean.TRUE);

        when(apiClient.crearBannerAdmin(anyString(), any(), anyString(), any(),
                anyBoolean(), anyInt(), anyString())).thenReturn(true);

        mockMvc.perform(post("/admin/banners/crear").session(session)
                        .param("titulo", "Promo")
                        .param("imagenUrl", "https://img.com/x.jpg"))
                .andExpect(status().is3xxRedirection());
    }
}
