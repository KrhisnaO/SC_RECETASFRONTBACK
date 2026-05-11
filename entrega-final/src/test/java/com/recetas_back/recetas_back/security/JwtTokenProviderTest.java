package com.recetas_back.recetas_back.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtTokenProvider - pruebas unitarias")
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret",
                "TestSecretKeyWithEnoughLengthForHmacShaAlgorithm1234567890");
        ReflectionTestUtils.setField(tokenProvider, "accessTokenExpirationMs", 3600000L);
        ReflectionTestUtils.setField(tokenProvider, "refreshTokenExpirationMs", 604800000L);
        ReflectionTestUtils.invokeMethod(tokenProvider, "init");

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

    @Test @DisplayName("generateRefreshToken: refresh token es válido y reconocible")
    void generateRefreshToken_esValidoYDetectable() {
        String refresh = tokenProvider.generateRefreshToken("maria");
        assertThat(tokenProvider.validateToken(refresh)).isTrue();
        assertThat(tokenProvider.isRefreshToken(refresh)).isTrue();
        assertThat(tokenProvider.getUsernameFromJWT(refresh)).isEqualTo("maria");
    }

    @Test @DisplayName("isRefreshToken: access token NO se reconoce como refresh")
    void isRefreshToken_accessTokenEsFalse() {
        String access = tokenProvider.generateAccessToken("maria");
        assertThat(tokenProvider.isRefreshToken(access)).isFalse();
    }

    @Test @DisplayName("isRefreshToken: token inválido retorna false sin lanzar")
    void isRefreshToken_invalidoEsFalse() {
        assertThat(tokenProvider.isRefreshToken("xxx.yyy.zzz")).isFalse();
    }

    @Test @DisplayName("init: cuando jwtSecret está vacío usa una clave generada")
    void init_secretVacioGeneraClave() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "");
        ReflectionTestUtils.setField(provider, "accessTokenExpirationMs", 60000L);
        ReflectionTestUtils.setField(provider, "refreshTokenExpirationMs", 120000L);
        ReflectionTestUtils.invokeMethod(provider, "init");
        String token = provider.generateAccessToken("test");
        assertThat(provider.validateToken(token)).isTrue();
    }
}
