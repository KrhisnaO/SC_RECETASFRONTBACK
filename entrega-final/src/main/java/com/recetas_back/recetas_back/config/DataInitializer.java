package com.recetas_back.recetas_back.config;

import com.recetas_back.recetas_back.model.Usuario;
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
}
