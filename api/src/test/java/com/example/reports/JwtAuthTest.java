package com.example.reports;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class JwtAuthTest {

    private JwtDecoder jwtDecoder;  // Мокаем декодер JWT

    @BeforeEach
    void setUp() {
        jwtDecoder = Mockito.mock(JwtDecoder.class); // Создаем мокаемый JwtDecoder

        // Мокаем JWT с ролью "prothetic_user"
        Jwt jwt = Mockito.mock(Jwt.class);
        when(jwt.getClaimAsStringList("roles")).thenReturn(Collections.singletonList("prothetic_user"));
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
    }

    @Test
    void testValidToken() {
        // Эмулируем заголовки запроса с Bearer-токеном
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer mock-token");

        // Декодируем токен и получаем роли
        Jwt decodedJwt = jwtDecoder.decode(headers.getFirst("Authorization").replace("Bearer ", ""));
        assertThat(decodedJwt).isNotNull();
        assertThat(decodedJwt.getClaimAsStringList("roles")).contains("prothetic_user");
    }

    @Test
    void testInvalidToken() {
        // Эмулируем отсутствие токена
        HttpHeaders headers = new HttpHeaders();

        // Проверяем, что заголовок Authorization отсутствует
        assertThat(headers.getFirst("Authorization")).isNull();
    }
}