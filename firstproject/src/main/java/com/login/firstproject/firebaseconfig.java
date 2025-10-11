package com.login.firstproject;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
@Configuration
public class firebaseconfig {

    @SuppressWarnings("deprecation")
    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        String keyPath = System.getenv("firebase");
        if (keyPath == null || keyPath.isEmpty()) {
            throw new IOException("Missing GOOGLE_APPLICATION_CREDENTIALS environment variable");
        }
        FileInputStream serviceAccount =
                new FileInputStream(keyPath);
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        return FirebaseApp.initializeApp(options);
    }
}

