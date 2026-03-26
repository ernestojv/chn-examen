package com.ernesto.chn_examen_api.service;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(
                jwtService,
                "secret",
                "this-is-a-very-long-test-secret-key-1234567890"
        );
    }

    @Test
    void generateToken_shouldCreateTokenForUsername() {
        String token = jwtService.generateToken("admin");

        assertNotNull(token);
        assertEquals("admin", jwtService.extractUsername(token));
    }

    @Test
    void generateToken_withExtraClaims_shouldStillContainSubject() {
        String token = jwtService.generateToken(Map.of("role", "USER"), "john");

        assertNotNull(token);
        assertEquals("john", jwtService.extractUsername(token));
    }

    @Test
    void isTokenValid_shouldReturnTrueForMatchingUser() {
        String token = jwtService.generateToken("john");
        UserDetails userDetails = User.withUsername("john").password("x").roles("USER").build();

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_shouldReturnFalseForDifferentUser() {
        String token = jwtService.generateToken("john");
        UserDetails userDetails = User.withUsername("mary").password("x").roles("USER").build();

        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void extractUsername_shouldThrowForInvalidToken() {
        assertThrows(JwtException.class, () -> jwtService.extractUsername("invalid-token"));
    }
}
