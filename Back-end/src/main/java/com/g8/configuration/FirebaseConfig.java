package com.g8.configuration;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

@Configuration
public class FirebaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    public FirebaseAuth firebaseAuth() {
        try {
            
            FirebaseOptions options = FirebaseOptions.builder().setCredentials(GoogleCredentials.getApplicationDefault()).build();
            FirebaseApp.initializeApp(options);
            return FirebaseAuth.getInstance();

        } catch (FileNotFoundException e) {
            logger.error("Firebase config file not found: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Error reading firebase config file: " + e.getMessage());
        }
        return null;
    }
}
