package org.example.springtestweb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.springtestweb.mapper.UserMapper;
import org.example.springtestweb.model.User;
import org.example.springtestweb.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(10);

    private JwtUtil jwtUtil;
    private UserMapper userMapper;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        userMapper = mock(UserMapper.class);
        authController = new AuthController();
        try {
            var jwtField = AuthController.class.getDeclaredField("jwtUtil");
            jwtField.setAccessible(true);
            jwtField.set(authController, jwtUtil);

            var mapperField = AuthController.class.getDeclaredField("userMapper");
            mapperField.setAccessible(true);
            mapperField.set(authController, userMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private User mockUser(String username, String plainPassword, String roles) {
        User user = new User();
        user.setId(1L);
        user.setName(username);
        user.setEmail(username + "@test.com");
        user.setUsername(username);
        user.setPasswordHash(ENCODER.encode(plainPassword));
        user.setRoles(roles);
        return user;
    }

    @Test
    void login_validCredentials_returnsJwtToken() {
        User admin = mockUser("admin", "123456", "ROLE_ADMIN");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(admin);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        when(jwtUtil.generateToken(anyLong(), anyList())).thenReturn("mocked.jwt.token");

        var req = new AuthController.LoginRequest();
        req.setUsername("admin");
        req.setPassword("123456");

        ResponseEntity<AuthController.LoginResponse> resp = authController.login(req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("mocked.jwt.token", resp.getBody().getToken());
        verify(jwtUtil).generateToken(anyLong(), anyList());
    }

    @Test
    void login_invalidPassword_returns401() {
        User admin = mockUser("admin", "123456", "ROLE_ADMIN");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(admin);

        var req = new AuthController.LoginRequest();
        req.setUsername("admin");
        req.setPassword("wrongpassword");

        ResponseEntity<AuthController.LoginResponse> resp = authController.login(req);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void login_userNotFound_returns401() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        var req = new AuthController.LoginRequest();
        req.setUsername("nobody");
        req.setPassword("123456");

        ResponseEntity<AuthController.LoginResponse> resp = authController.login(req);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void login_nullFields_returns401() {
        var req = new AuthController.LoginRequest();

        ResponseEntity<AuthController.LoginResponse> resp = authController.login(req);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        verifyNoInteractions(jwtUtil);
        verifyNoInteractions(userMapper);
    }
}
