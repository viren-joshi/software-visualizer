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
						boolean result = file.delete();
						if(result)
							System.out.println("File deleted!");
						else
							System.out.println("Couldn't delete the file!");
					}
				} catch (Exception e) {
					System.err.println("Failed to delete file on shutdown: " + e.getMessage());
				}
			}
		}));
	}
}
