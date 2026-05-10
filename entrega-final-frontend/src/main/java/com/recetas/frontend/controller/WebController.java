package com.recetas.frontend.controller;

import com.recetas.frontend.dto.*;
import com.recetas.frontend.service.ApiClient;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class WebController {

    @Autowired
    private ApiClient apiClient;

    // Paginas publicas

    @GetMapping({"/", "/home", "/recetas"})
    public String home(Model model) {
        model.addAttribute("recetas", apiClient.obtenerRecetas());
        return "home";
    }

    @GetMapping("/buscar")
    public String buscar(@RequestParam(required = false) String query,
                         @RequestParam(required = false) String tipoCocina,
                         @RequestParam(required = false) String pais,
                         @RequestParam(required = false) String dificultad,
                         Model model) {
        model.addAttribute("recetas", apiClient.buscarRecetas(query, tipoCocina, pais, dificultad));
        return "buscar";
    }

    @GetMapping("/receta/{id}")
    public String detalle(@PathVariable Long id, Model model, HttpSession session) {
        RecetaDTO receta = apiClient.obtenerReceta(id);
        if (receta == null) return "redirect:/home";

        List<ComentarioDTO> comentarios = apiClient.obtenerComentarios(id);
        ValoracionDTO valoracion        = apiClient.obtenerValoracion(id);

        String token     = (String) session.getAttribute("JWT_TOKEN");
        boolean esFavorito = token != null && apiClient.esFavorito(id, token);

        model.addAttribute("receta",           receta);
        model.addAttribute("comentarios",      comentarios);
        model.addAttribute("promedioValoracion", valoracion.getPromedio());
        model.addAttribute("totalValoraciones",  valoracion.getTotal());
        model.addAttribute("esFavorito",         esFavorito);
        return "detalle";
    }

    // Login / Logout

    @GetMapping("/login")
    public String loginForm() { return "login"; }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session, Model model) {
        ApiClient.LoginResult result = apiClient.login(username, password);
        if (result != null) {
            session.setAttribute("JWT_TOKEN",  result.token());
            session.setAttribute("USERNAME",   username);
            session.setAttribute("IS_ADMIN",   "ROLE_ADMIN".equals(result.role()));
            return "redirect:/home";
        }
        model.addAttribute("error", true);
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }

    // Registro

    @GetMapping("/registro")
    public String registroForm() { return "registro"; }

    @PostMapping("/registro")
    public String registro(@RequestParam String username,
                           @RequestParam(required = false) String nombre,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String passwordConfirm,
                           Model model) {
        if (!password.equals(passwordConfirm)) {
            model.addAttribute("error", "Las contrasenas no coinciden.");
            model.addAttribute("usuarioIngresado", username);
            model.addAttribute("nombreIngresado",  nombre);
            model.addAttribute("emailIngresado",   email);
            return "registro";
        }
        String errorMsg = apiClient.registrar(username, nombre, email, password);
        if (errorMsg == null) return "redirect:/login?registrado";
        model.addAttribute("error",            errorMsg);
        model.addAttribute("usuarioIngresado", username);
        model.addAttribute("nombreIngresado",  nombre);
        model.addAttribute("emailIngresado",   email);
        return "registro";
    }

    // Publicar receta

    @GetMapping("/publicar")
    public String publicarForm(HttpSession session) {
        if (session.getAttribute("JWT_TOKEN") == null) return "redirect:/login";
        return "publicar";
    }

    @PostMapping("/publicar")
    public String publicar(@RequestParam String nombre,
                           @RequestParam(required = false) String tipoCocina,
                           @RequestParam(required = false) String pais,
                           @RequestParam(required = false) String dificultad,
                           @RequestParam(required = false) Integer tiempoPrepMinutos,
                           @RequestParam(required = false) String descripcion,
                           @RequestParam(required = false) String instrucciones,
                           @RequestParam(required = false) String ingredientes,
                           @RequestParam(required = false) String imagenUrl,
                           HttpSession session, Model model) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return "redirect:/login";

        String error = apiClient.publicarReceta(nombre, tipoCocina, pais, dificultad,
                tiempoPrepMinutos, descripcion, instrucciones, ingredientes, imagenUrl, token);
        if (error == null) return "redirect:/home?publicado";
        model.addAttribute("error", error);
        return "publicar";
    }

    // Favoritos

    @GetMapping("/favoritos")
    public String favoritos(HttpSession session, Model model) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return "redirect:/login";
        model.addAttribute("recetas", apiClient.obtenerFavoritos(token));
        return "favoritos";
    }

    @PostMapping("/favoritos/{id}/agregar")
    public String agregarFavorito(@PathVariable Long id, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token != null) apiClient.agregarFavorito(id, token);
        return "redirect:/receta/" + id;
    }

    @PostMapping("/favoritos/{id}/eliminar")
    public String eliminarFavorito(@PathVariable Long id, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token != null) apiClient.eliminarFavorito(id, token);
        return "redirect:/receta/" + id;
    }

    // Comentar / Valorar

    @PostMapping("/receta/{id}/comentar")
    public String comentar(@PathVariable Long id, @RequestParam String contenido, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token != null) apiClient.publicarComentario(id, contenido, token);
        return "redirect:/receta/" + id;
    }

    @PostMapping("/receta/{id}/valorar")
    public String valorar(@PathVariable Long id, @RequestParam Integer puntuacion, HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token != null) apiClient.valorar(id, puntuacion, token);
        return "redirect:/receta/" + id;
    }

    // Panel Admin

    @GetMapping("/admin")
    public String adminPanel(HttpSession session, Model model) {
        String token   = (String)  session.getAttribute("JWT_TOKEN");
        Boolean isAdmin = (Boolean) session.getAttribute("IS_ADMIN");
        if (token == null || !Boolean.TRUE.equals(isAdmin)) return "redirect:/home";

        model.addAttribute("usuarios",    apiClient.listarUsuariosAdmin(token));
        model.addAttribute("comentarios", apiClient.listarComentariosAdmin(token));
        model.addAttribute("recetas",     apiClient.listarRecetasAdmin(token));
        return "admin";
    }

    @PostMapping("/admin/comentarios/{id}/eliminar")
    public String eliminarComentarioAdmin(@PathVariable Long id, HttpSession session) {
        String token   = (String)  session.getAttribute("JWT_TOKEN");
        Boolean isAdmin = (Boolean) session.getAttribute("IS_ADMIN");
        if (token == null || !Boolean.TRUE.equals(isAdmin)) return "redirect:/home";
        apiClient.eliminarComentarioAdmin(id, token);
        return "redirect:/admin#comentarios";
    }

    @PostMapping("/admin/usuarios/{id}/eliminar")
    public String eliminarUsuarioAdmin(@PathVariable Long id, HttpSession session) {
        String token   = (String)  session.getAttribute("JWT_TOKEN");
        Boolean isAdmin = (Boolean) session.getAttribute("IS_ADMIN");
        if (token == null || !Boolean.TRUE.equals(isAdmin)) return "redirect:/home";
        apiClient.eliminarUsuarioAdmin(id, token);
        return "redirect:/admin#usuarios";
    }

    @PostMapping("/admin/usuarios/{id}/cambiar-rol")
    public String cambiarRolAdmin(@PathVariable Long id,
                                  @RequestParam String role,
                                  HttpSession session) {
        String token   = (String)  session.getAttribute("JWT_TOKEN");
        Boolean isAdmin = (Boolean) session.getAttribute("IS_ADMIN");
        if (token == null || !Boolean.TRUE.equals(isAdmin)) return "redirect:/home";
        apiClient.cambiarRolAdmin(id, role, token);
        return "redirect:/admin#usuarios";
    }

    @PostMapping("/admin/recetas/{id}/eliminar")
    public String eliminarRecetaAdmin(@PathVariable Long id, HttpSession session) {
        String token   = (String)  session.getAttribute("JWT_TOKEN");
        Boolean isAdmin = (Boolean) session.getAttribute("IS_ADMIN");
        if (token == null || !Boolean.TRUE.equals(isAdmin)) return "redirect:/home";
        apiClient.eliminarRecetaAdmin(id, token);
        return "redirect:/admin#recetas";
    }

    // Perfil

    @GetMapping("/perfil")
    public String perfilForm(HttpSession session, Model model) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return "redirect:/login";
        Map<String, Object> perfil = apiClient.obtenerPerfil(token);
        if (perfil == null) return "redirect:/login";
        model.addAttribute("perfil", perfil);
        return "perfil";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@RequestParam String nombre,
                                   @RequestParam String correo,
                                   HttpSession session, Model model) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return "redirect:/login";
        String error = apiClient.actualizarPerfil(nombre, correo, token);
        if (error != null) {
            Map<String, Object> perfil = apiClient.obtenerPerfil(token);
            model.addAttribute("perfil",      perfil);
            model.addAttribute("errorPerfil", error);
            return "perfil";
        }
        return "redirect:/perfil?actualizado";
    }

    @PostMapping("/perfil/cambiar-password")
    public String cambiarPassword(@RequestParam String passwordActual,
                                  @RequestParam String passwordNueva,
                                  @RequestParam String passwordNuevaConfirm,
                                  HttpSession session, Model model) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) return "redirect:/login";

        Map<String, Object> perfil = apiClient.obtenerPerfil(token);
        model.addAttribute("perfil", perfil);

        if (!passwordNueva.equals(passwordNuevaConfirm)) {
            model.addAttribute("errorPassword", "Las contrasenas nuevas no coinciden.");
            return "perfil";
        }
        if (passwordNueva.length() < 6) {
            model.addAttribute("errorPassword", "La nueva contrasena debe tener al menos 6 caracteres.");
            return "perfil";
        }
        String error = apiClient.cambiarPassword(passwordActual, passwordNueva, token);
        if (error != null) {
            model.addAttribute("errorPassword", error);
            return "perfil";
        }
        return "redirect:/perfil?passwordCambiado";
    }
}