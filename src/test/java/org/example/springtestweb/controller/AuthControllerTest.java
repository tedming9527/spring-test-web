package org.example.springtestweb.controller;

import org.example.springtestweb.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private JwtUtil jwtUtil;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        authController = new AuthController();
        // inject via reflection since @Autowired isn't active in unit tests
        try {
            var field = AuthController.class.getDeclaredField("jwtUtil");
            field.setAccessible(true);
            field.set(authController, jwtUtil);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void login_validCredentials_returnsJwtToken() {
        when(jwtUtil.generateToken(anyLong(), anyList())).thenReturn("mocked.jwt.token");

        var req = new AuthController.LoginRequest();
        req.setUsername("admin");
        req.setPassword("123456");

        ResponseEntity<AuthController.LoginResponse> resp = authController.login(req, null);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("mocked.jwt.token", resp.getBody().getToken());
        verify(jwtUtil).generateToken(anyLong(), anyList());
    }

    @Test
    void login_invalidCredentials_returns401() {
        var req = new AuthController.LoginRequest();
        req.setUsername("admin");
        req.setPassword("wrong");

        ResponseEntity<AuthController.LoginResponse> resp = authController.login(req, null);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void login_missingFields_returns401() {
        var req = new AuthController.LoginRequest();
        // username and password are null

        ResponseEntity<AuthController.LoginResponse> resp = authController.login(req, null);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        verifyNoInteractions(jwtUtil);
    }
}

