package com.g8.service;

import com.google.firebase.ErrorCode;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private FirebaseAuth firebaseAuth;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(firebaseAuth);
    }

    @Test
    void testSignUpSuccess() throws FirebaseAuthException {
        UserRecord mockUserRecord = mock(UserRecord.class);
        when(mockUserRecord.getUid()).thenReturn("testUid");
        when(firebaseAuth.createUser(any(CreateRequest.class))).thenReturn(mockUserRecord);

        String response = authService.signUp("test@example.com", "password123", "Test User");
        
        assertTrue(response.contains("testUid"));
    }

    @Test
    void testSignUpFailure() throws FirebaseAuthException {

        FirebaseException firebaseException = new FirebaseException(ErrorCode.UNKNOWN, "Failed", null);
        FirebaseAuthException firebaseAuthException = new FirebaseAuthException(firebaseException);

        when(firebaseAuth.createUser(any(CreateRequest.class))).thenThrow(firebaseAuthException);

        assertThrows(RuntimeException.class, () -> authService.signUp("test@example.com", "password123", "Test User"));
    }

    @Test
    void testVerifyTokenSuccess() throws FirebaseAuthException {
        FirebaseToken mockToken = mock(FirebaseToken.class);
        when(mockToken.getUid()).thenReturn("testUid");
        when(firebaseAuth.verifyIdToken("validToken")).thenReturn(mockToken);

        boolean isVerified = authService.verifyToken("validToken");
        
        assertTrue(isVerified);
    }

    @Test
    void testVerifyTokenFailure() throws FirebaseAuthException {

        FirebaseException firebaseException = new FirebaseException(ErrorCode.PERMISSION_DENIED, "Failed", null);
        FirebaseAuthException firebaseAuthException = new FirebaseAuthException(firebaseException);

        when(firebaseAuth.verifyIdToken("invalidToken")).thenThrow(firebaseAuthException);

        assertThrows(RuntimeException.class, () -> authService.verifyToken("invalidToken"));
    }
}
