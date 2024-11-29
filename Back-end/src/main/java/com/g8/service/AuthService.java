package com.g8.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    private final FirebaseAuth firebaseAuth;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final Gson gson = new Gson();
    
    public AuthService(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    public String signUp(String email, String password, String name) throws RuntimeException {

       CreateRequest createRequest = new CreateRequest().setEmail(email).setDisplayName(name).setPassword(password);
        try {
            UserRecord userRecord = firebaseAuth.createUser(createRequest);
            logger.info("Successfully created new user: " + userRecord.getUid());
            Map<String, String> response = new HashMap<>();
            response.put("uid", userRecord.getUid());
            return gson.toJson(response);

        } catch (FirebaseAuthException e) {
            logger.error("Error creating user: " + e.getMessage());
            throw new RuntimeException("Error while saving user in firestore");
        }
    }

    public boolean verifyToken(String idToken) throws RuntimeException {
        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            logger.info("User " + decodedToken.getUid() + " is authenticated.");
            return true;
        } catch (FirebaseAuthException e) {
            logger.error("Error verifying token: " + e.getMessage());
            throw new RuntimeException("Error while verifying the user token!");
        }
    }

    public String getUserId(String idToken) throws RuntimeException {

        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            return decodedToken.getUid();
        } catch (FirebaseAuthException e) {
            logger.error("Error verifying token: " + e.getMessage());
            throw new RuntimeException("Error while retrieving user Id for verification");
        }
    }
}
