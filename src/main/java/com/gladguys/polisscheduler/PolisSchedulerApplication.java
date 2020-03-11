package com.gladguys.polisscheduler;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.io.InputStream;

@SpringBootApplication
public class PolisSchedulerApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(PolisSchedulerApplication.class, args);
	}

	@Bean
	public Firestore getFirestore() throws IOException {
		InputStream serviceAccount = new ClassPathResource("polis-7d367-firebase-adminsdk-io5c2-a91fa30f53.json").getInputStream();
		GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
		FirebaseOptions options = new FirebaseOptions.Builder()
				.setCredentials(credentials)
				.build();
		FirebaseApp.initializeApp(options);

		return FirestoreClient.getFirestore();
	}
}
