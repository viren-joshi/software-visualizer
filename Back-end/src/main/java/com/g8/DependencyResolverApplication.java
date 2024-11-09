package com.g8;

import com.g8.utils.FileProps;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class DependencyResolverApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DependencyResolverApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		// Add a shutdown hook to delete a file on exit
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {

			if(FileProps.getFilePath() != null) {

				try {
					File file = new File(FileProps.getFilePath());
					if (file.exists()) {
						file.delete();
						System.out.println("File deleted!");
					}
				} catch (Exception e) {
					System.err.println("Failed to delete file on shutdown: " + e.getMessage());
				}
			}
		}));
	}
}
