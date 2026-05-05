package com.recetas_back.recetas_back.service;

import com.recetas_back.recetas_back.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - pruebas unitarias")
class AuthServiceTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock JwtTokenProvider tokenProvider;
    @Mock Authentication authentication;
    @InjectMocks AuthService authService;

    @Test @DisplayName("autenticar retorna token JWT con credenciales correctas")
    void autenticar_retornaToken() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("eyJ.fake.token");

        String token = authService.autenticar("admin", "password");

        assertThat(token).isEqualTo("eyJ.fake.token");
        verify(authenticationManager).authenticate(any());
        verify(tokenProvider).generateToken(authentication);
    }

    @Test @DisplayName("autenticar lanza excepción con credenciales incorrectas")
    void autenticar_lanzaExcepcion() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Inválidas"));
        assertThatThrownBy(() -> authService.autenticar("admin", "wrong"))
                .isInstanceOf(BadCredentialsException.class);
    }
}
