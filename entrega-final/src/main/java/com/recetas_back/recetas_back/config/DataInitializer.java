package com.recetas_back.recetas_back.config;

import com.recetas_back.recetas_back.model.Banner;
import com.recetas_back.recetas_back.model.Usuario;
import com.recetas_back.recetas_back.repository.BannerRepository;
import com.recetas_back.recetas_back.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.count() == 0) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("PasswordAdmin1"));
                admin.setRole("ROLE_ADMIN");
                usuarioRepository.save(admin);

                Usuario carlos = new Usuario();
                carlos.setUsername("carlos");
                carlos.setPassword(passwordEncoder.encode("PasswordUser1"));
                carlos.setRole("ROLE_EDITOR");
                usuarioRepository.save(carlos);

                Usuario maria = new Usuario();
                maria.setUsername("maria");
                maria.setPassword(passwordEncoder.encode("PasswordUser2"));
                maria.setRole("ROLE_USER");
                usuarioRepository.save(maria);
            }
        };
    }

    @Bean
    CommandLineRunner initBanners(BannerRepository bannerRepository) {
        return args -> {
            if (bannerRepository.count() == 0) {
                Banner b1 = new Banner();
                b1.setTitulo("Utensilios profesionales con 30% de descuento");
                b1.setEmpresa("CocinaPro");
                b1.setImagenUrl("https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=1200&q=80");
                b1.setEnlaceUrl("https://example.com/cocinapro");
                b1.setActivo(true);
                b1.setOrden(1);
                bannerRepository.save(b1);

                Banner b2 = new Banner();
                b2.setTitulo("Ingredientes orgánicos a tu puerta");
                b2.setEmpresa("HuertoFresco");
                b2.setImagenUrl("https://images.unsplash.com/photo-1542838132-92c53300491e?w=1200&q=80");
                b2.setEnlaceUrl("https://example.com/huertofresco");
                b2.setActivo(true);
                b2.setOrden(2);
                bannerRepository.save(b2);

                Banner b3 = new Banner();
                b3.setTitulo("Cursos online de gastronomía internacional");
                b3.setEmpresa("ChefAcademy");
                b3.setImagenUrl("https://images.unsplash.com/photo-1466637574441-749b8f19452f?w=1200&q=80");
                b3.setEnlaceUrl("https://example.com/chefacademy");
                b3.setActivo(true);
                b3.setOrden(3);
                bannerRepository.save(b3);
            }
        };
    }
}
