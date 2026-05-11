package com.recetas_back.recetas_back.service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class ModeracionService {

    private static final List<String> PALABRAS_PROHIBIDAS = Arrays.asList(
            "mierda", "puto", "puta", "imbecil", "idiota", "estupido", "estupida",
            "pendejo", "pendeja", "maricon", "perra", "cabron", "cabrona",
            "nazi", "racista", "homofobo",
            "viagra", "casino", "porno",
            "hackear", "piratear"
    );

    public String motivoRechazoSiHay(String contenido) {
        if (contenido == null || contenido.isBlank()) {
            return "El comentario está vacío.";
        }
        String normalizado = normalizar(contenido);
        for (String palabra : PALABRAS_PROHIBIDAS) {
            if (normalizado.matches(".*\\b" + java.util.regex.Pattern.quote(palabra) + "\\b.*")) {
                return "Contiene lenguaje no permitido (palabra prohibida).";
            }
        }
        if (contenido.length() > 1000) {
            return "El comentario excede el largo máximo permitido (1000 caracteres).";
        }
        return null;
    }

    private String normalizar(String texto) {
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return sinAcentos.toLowerCase(Locale.ROOT);
    }
}
