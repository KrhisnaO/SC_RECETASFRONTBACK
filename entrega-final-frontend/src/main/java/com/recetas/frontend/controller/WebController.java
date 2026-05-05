package com.recetas.frontend.controller;

import com.recetas.frontend.dto.*;
import com.recetas.frontend.service.ApiClient;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador Thymeleaf del FRONTEND.
 *
 * Responsabilidades:
 *   1. Recibir petición HTTP del navegador
 *   2. Llamar a ApiClient para obtener datos del backend
 *   3. Pasar los datos al modelo Thymeleaf
 *   4. Devolver el nombre de la vista a renderizar
 *
 * NO accede a ningún repositorio ni base de datos directamente.
 * Toda la lógica de negocio y de datos está en el BACKEND.
 */
@Controller
public class WebController {

    @Autowired
    private ApiClient apiClient;

    // ─── Páginas públicas ────────────────────────────────────────────────

    @GetMapping({"/", "/home", "/recetas"})
    public String home(Model model) {
        List<RecetaDTO> recetas = apiClient.obtenerRecetas();
        model.addAttribute("recetas", recetas);
        return "home";
    }

    @GetMapping("/buscar")
    public String buscar(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tipoCocina,
            @RequestParam(required = false) String pais,
            @RequestParam(required = false) String dificultad,
            Model model) {
        List<RecetaDTO> resultados = apiClient.buscarRecetas(query, tipoCocina, pais, dificultad);
        model.addAttribute("recetas", resultados);
        return "buscar";
    }

    @GetMapping("/receta/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        RecetaDTO receta = apiClient.obtenerReceta(id);
        if (receta == null) return "redirect:/home";

        List<ComentarioDTO> comentarios = apiClient.obtenerComentarios(id);
        ValoracionDTO valoracion = apiClient.obtenerValoracion(id);

        model.addAttribute("receta", receta);
        model.addAttribute("comentarios", comentarios);
        model.addAttribute("promedioValoracion", valoracion.getPromedio());
        model.addAttribute("totalValoraciones", valoracion.getTotal());
        return "detalle";
    }

    // ─── Login / Logout ──────────────────────────────────────────────────

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        String token = apiClient.login(username, password);
        if (token != null) {
            session.setAttribute("JWT_TOKEN", token);
            session.setAttribute("USERNAME", username);
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

    // ─── Acciones autenticadas ───────────────────────────────────────────

    @PostMapping("/receta/{id}/comentar")
    public String comentar(@PathVariable Long id,
                           @RequestParam String contenido,
                           HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token != null) {
            apiClient.publicarComentario(id, contenido, token);
        }
        return "redirect:/receta/" + id;
    }

    @PostMapping("/receta/{id}/valorar")
    public String valorar(@PathVariable Long id,
                          @RequestParam Integer puntuacion,
                          HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token != null) {
            apiClient.valorar(id, puntuacion, token);
        }
        return "redirect:/receta/" + id;
    }
}
