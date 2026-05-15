package org.example.springtestweb.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        // Secret must be at least 32 chars for HS256
        jwtUtil = new JwtUtil("test-secret-key-at-least-32chars!!", 3600000L);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken(1L, List.of("ROLE_ADMIN"));
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtUtil.generateToken(1L, List.of("ROLE_ADMIN"));
        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void isTokenValid_invalidToken_returnsFalse() {
        assertFalse(jwtUtil.isTokenValid("not.a.valid.token"));
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() throws InterruptedException {
        JwtUtil shortLived = new JwtUtil("test-secret-key-at-least-32chars!!", 1L); // 1 ms TTL
        String token = shortLived.generateToken(1L, List.of("ROLE_USER"));
        Thread.sleep(50);
        assertFalse(shortLived.isTokenValid(token));
    }

    @Test
    void isTokenValid_wrongSignature_returnsFalse() {
        JwtUtil other = new JwtUtil("different-secret-key-at-least-32!!", 3600000L);
        String token = other.generateToken(1L, List.of("ROLE_USER"));
        assertFalse(jwtUtil.isTokenValid(token));
    }

    @Test
    void getClaims_containsSubjectAndRoles() {
        String token = jwtUtil.generateToken(42L, List.of("ROLE_ADMIN", "ROLE_USER"));
        Map<String, Object> claims = jwtUtil.getClaims(token);
        assertEquals("42", claims.get("sub"));
        Object roles = claims.get("roles");
        assertNotNull(roles);
        assertTrue(roles.toString().contains("ROLE_ADMIN"));
        assertTrue(roles.toString().contains("ROLE_USER"));
    }
}
