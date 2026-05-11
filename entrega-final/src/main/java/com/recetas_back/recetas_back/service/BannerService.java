package com.recetas_back.recetas_back.service;

import com.recetas_back.recetas_back.model.Banner;
import com.recetas_back.recetas_back.repository.BannerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerService {

    @Autowired
    private BannerRepository bannerRepository;

    public List<Banner> listarActivos() {
        return bannerRepository.findByActivoTrueOrderByOrdenAscIdAsc();
    }

    public List<Banner> listarTodos() {
        return bannerRepository.findAll();
    }

    public Banner crear(String titulo, String empresa, String imagenUrl, String enlaceUrl,
                        Boolean activo, Integer orden) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título del banner es obligatorio.");
        }
        if (imagenUrl == null || imagenUrl.isBlank()) {
            throw new IllegalArgumentException("La URL de la imagen es obligatoria.");
        }
        Banner b = new Banner();
        b.setTitulo(titulo.trim());
        b.setEmpresa(empresa);
        b.setImagenUrl(imagenUrl.trim());
        b.setEnlaceUrl(enlaceUrl);
        b.setActivo(activo != null ? activo : Boolean.TRUE);
        b.setOrden(orden != null ? orden : 0);
        return bannerRepository.save(b);
    }

    public Banner actualizar(Long id, String titulo, String empresa, String imagenUrl,
                             String enlaceUrl, Boolean activo, Integer orden) {
        Banner b = bannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Banner no encontrado: " + id));
        if (titulo != null && !titulo.isBlank()) b.setTitulo(titulo.trim());
        if (empresa != null) b.setEmpresa(empresa);
        if (imagenUrl != null && !imagenUrl.isBlank()) b.setImagenUrl(imagenUrl.trim());
        if (enlaceUrl != null) b.setEnlaceUrl(enlaceUrl);
        if (activo != null) b.setActivo(activo);
        if (orden != null) b.setOrden(orden);
        return bannerRepository.save(b);
    }

    public void eliminar(Long id) {
        if (!bannerRepository.existsById(id)) {
            throw new IllegalArgumentException("Banner no encontrado: " + id);
        }
        bannerRepository.deleteById(id);
    }
}
