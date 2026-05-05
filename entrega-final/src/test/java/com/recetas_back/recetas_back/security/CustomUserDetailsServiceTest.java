package com.recetas_back.recetas_back.security;

import com.recetas_back.recetas_back.model.Usuario;
import com.recetas_back.recetas_back.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService - pruebas unitarias")
class CustomUserDetailsServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks CustomUserDetailsService userDetailsService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L); usuario.setUsername("admin");
        usuario.setPassword("$2a$10$hashed"); usuario.setRole("ROLE_ADMIN");
    }

    @Test @DisplayName("loadUserByUsername retorna UserDetails si existe")
    void loadUserByUsername_retornaUserDetails() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        UserDetails result = userDetailsService.loadUserByUsername("admin");
        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Test @DisplayName("loadUserByUsername lanza excepción si no existe")
    void loadUserByUsername_lanzaExcepcion() {
        when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("fantasma"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("fantasma");
    }
}
