package com.g8.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.firebase.auth.FirebaseAuth;

@Service
public class AuthService {
    public AuthService(FirebaseAuth firebaseAuth) {
        // Constructor
    }

    public ResponseEntity<String> signUp(String email, String password, String name) {
        // Skeleton for signUp method
        return null;
    }

    public boolean verifyToken(String idToken) {
        // Skeleton for verifyToken method
        return false;
    }
}
