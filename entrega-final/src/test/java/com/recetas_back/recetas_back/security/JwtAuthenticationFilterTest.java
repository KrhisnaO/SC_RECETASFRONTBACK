package com.recetas_back.recetas_back.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("JwtAuthenticationFilter - pruebas unitarias")
class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private JwtTokenProvider tokenProvider;
    private UserDetailsService userDetailsService;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        tokenProvider = mock(JwtTokenProvider.class);
        userDetailsService = mock(UserDetailsService.class);
        filterChain = mock(FilterChain.class);

        filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "tokenProvider", tokenProvider);
        ReflectionTestUtils.setField(filter, "customUserDetailsService", userDetailsService);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Token válido: setea autenticación en SecurityContext")
    void doFilter_tokenValido_seteaAutenticacion() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        UserDetails ud = new User("admin", "x", Collections.emptyList());
        when(tokenProvider.validateToken("token-valido")).thenReturn(true);
        when(tokenProvider.getUsernameFromJWT("token-valido")).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(ud);

        filter.doFilter(req, resp, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("admin");
        verify(filterChain).doFilter(req, resp);
    }

    @Test
    @DisplayName("Token inválido: NO setea autenticación pero continúa la cadena")
    void doFilter_tokenInvalido_noSeteaAutenticacion() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer token-invalido");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        when(tokenProvider.validateToken("token-invalido")).thenReturn(false);

        filter.doFilter(req, resp, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(req, resp);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("Sin header Authorization: continúa sin autenticación")
    void doFilter_sinHeader_continuaSinAuth() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse resp = new MockHttpServletResponse();

        filter.doFilter(req, resp, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(req, resp);
        verifyNoInteractions(tokenProvider);
    }

    @Test
    @DisplayName("Header sin prefijo Bearer: ignorado")
    void doFilter_headerSinBearer_ignorado() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Basic abcdef");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        filter.doFilter(req, resp, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(req, resp);
        verifyNoInteractions(tokenProvider);
    }

    @Test
    @DisplayName("Excepción al validar: filtro no propaga, sigue la cadena")
    void doFilter_excepcion_noRompeCadena() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer x");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        when(tokenProvider.validateToken("x")).thenThrow(new RuntimeException("boom"));

        filter.doFilter(req, resp, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(req, resp);
    }
}
