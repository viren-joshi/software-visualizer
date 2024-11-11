package com.g8.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.gson.Gson;

@Service
public class AuthService {
    private final FirebaseAuth firebaseAuth;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final Gson gson = new Gson();
    
    public AuthService(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    public ResponseEntity<String> signUp(String email, String password, String name) {
       CreateRequest createRequest = new CreateRequest().setEmail(email).setDisplayName(name).setPassword(password);
        try {
            UserRecord userRecord = firebaseAuth.createUser(createRequest);
            logger.info("Successfully created new user: " + userRecord.getUid());
            Map<String, String> response = new HashMap<>();
            response.put("uid", userRecord.getUid());
            return ResponseEntity.ok(gson.toJson(response));    
        
        } catch (FirebaseAuthException e) {
            logger.error("Error creating user: " + e.getMessage());
            return ResponseEntity.status(500).body("Error creating user: " + e.getMessage());
        }
    }

    public boolean verifyToken(String idToken) {
        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            logger.info("User " + decodedToken.getUid() + " is authenticated.");
            return true;
        } catch (FirebaseAuthException e) {
            logger.error("Error verifying token: " + e.getMessage());
            return false;
        }
    }
}
