package com.recetas_back.recetas_back.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtTokenProvider - pruebas unitarias")
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        UserDetails userDetails = new User("admin", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    @Test @DisplayName("generateToken retorna token no vacío")
    void generateToken_retornaToken() {
        assertThat(tokenProvider.generateToken(authentication)).isNotBlank();
    }

    @Test @DisplayName("getUsernameFromJWT extrae username correctamente")
    void getUsernameFromJWT_extraeUsername() {
        String token = tokenProvider.generateToken(authentication);
        assertThat(tokenProvider.getUsernameFromJWT(token)).isEqualTo("admin");
    }

    @Test @DisplayName("validateToken retorna true para token válido")
    void validateToken_retornaTrue() {
        String token = tokenProvider.generateToken(authentication);
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test @DisplayName("validateToken retorna false para token inválido")
    void validateToken_retornaFalse() {
        assertThat(tokenProvider.validateToken("token.invalido.falso")).isFalse();
    }

    @Test @DisplayName("validateToken retorna false para string vacío")
    void validateToken_retornaFalseVacio() {
        assertThat(tokenProvider.validateToken("")).isFalse();
    }
}
