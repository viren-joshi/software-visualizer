package com.g8.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.io.FileNotFoundException;
import java.io.IOException;
// Keeping these imports for running the project locally.
import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;
import java.io.FileInputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    public FirebaseAuth firebaseAuth() {

        try {
            /*
             * The code commented below is for running the project locally. It reads the [.env] file to get the path to the Firebase credentials file.
             * and the [.env] file should be in the root directory of the project. It should contain the variable
             * GOOGLE_APPLICATION_CREDENTIALS=path/to/firebase-config.json
             */
            // Dotenv dotenv = Dotenv.load();
            // String credentialsPath = dotenv.get("GOOGLE_APPLICATION_CREDENTIALS");
            // FileInputStream serviceAccount = new FileInputStream(System.getProperty("user.dir") + File.separator + credentialsPath);
            // FirebaseOptions options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();
            FirebaseOptions options = FirebaseOptions.builder().setCredentials(GoogleCredentials.getApplicationDefault()).build(); // Comment this out, if you are using the commented code above.
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
