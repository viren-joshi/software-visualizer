package com.g8.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    public FirebaseAuth firebaseAuth() {

        try {
            Dotenv dotenv = Dotenv.load();
            String credentialsPath = dotenv.get("GOOGLE_APPLICATION_CREDENTIALS");
            FileInputStream serviceAccount = new FileInputStream(System.getProperty("user.dir") + File.separator + credentialsPath);

            FirebaseOptions options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();
            FirebaseApp.initializeApp(options);
            return FirebaseAuth.getInstance();

        } catch (FileNotFoundException e) {
            logger.error("Firebase config file not found: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Error reading firebase config file: " + e.getMessage());
        }
        return null;
    }

    @Bean
    public Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }
}
