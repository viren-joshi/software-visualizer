package com.g8.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.g8.service.AuthService;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authController = new AuthController(authService);
    }

    @Test
    void testSignUpSuccess() {
        ResponseEntity<String> mockResponse = ResponseEntity.ok("{uid: 'testUid'}");
        when(authService.signUp("test@example.com", "password123", "Test User")).thenReturn(mockResponse);

        ResponseEntity<String> response = authController.signUp("test@example.com", "password123", "Test User");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{uid: 'testUid'}", response.getBody());
    }

    @Test
    void testSignUpFailure() {
        ResponseEntity<String> mockResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating user");
        when(authService.signUp("test@example.com", "password123", "Test User")).thenReturn(mockResponse);

        ResponseEntity<String> response = authController.signUp("test@example.com", "password123", "Test User");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error creating user", response.getBody());
    }

    @Test
    void testVerifyTokenSuccess() {
        when(authService.verifyToken("validToken")).thenReturn(true);

        ResponseEntity<String> response = authController.verifyToken("validToken");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{isVerified: true}", response.getBody());
    }

    @Test
    void testVerifyTokenFailure() {
        when(authService.verifyToken("invalidToken")).thenReturn(false);

        ResponseEntity<String> response = authController.verifyToken("invalidToken");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("{isVerified: false}", response.getBody());
    }
}
