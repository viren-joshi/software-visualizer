package com.g8.controller;

import com.g8.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

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

        when(authService.signUp("test@example.com", "password123", "Test User")).thenReturn(mockResponse.getBody());

        ResponseEntity<String> response = authController.signUp("test@example.com", "password123", "Test User");

        assertEquals(mockResponse, response);
    }

    @Test
    void testSignUpFailure() throws Exception {
        when(authService.signUp("test@example.com", "password123", "Test User"))
                .thenThrow(new RuntimeException("mock exception"));

        ResponseEntity<String> response = authController.signUp("test@example.com", "password123", "Test User");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
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
