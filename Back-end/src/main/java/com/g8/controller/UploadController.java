package com.g8.controller;

import com.g8.service.AnalyzeProjectService;
import com.g8.service.AuthService;
import com.g8.service.DependencyRetrievalService;
import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/initialize")
@CrossOrigin(origins = "*")
public class UploadController {

    private final AnalyzeProjectService analyzeProjectService;
    private final AuthService authService;
    private final DependencyRetrievalService dependencyRetrievalService;

    @Autowired
    public UploadController(AnalyzeProjectService analyzeProjectService, AuthService authService, DependencyRetrievalService dependencyRetrievalService) {
        this.analyzeProjectService = analyzeProjectService;
        this.authService = authService;
        this.dependencyRetrievalService = dependencyRetrievalService;
    }

    // Saves the uploaded file and analyzes it
    @PostMapping("/upload")
    public ResponseEntity<String> uploadProject(@RequestParam("file") MultipartFile file,
                                                @RequestParam("classContainer") String classContainer,
                                                @RequestHeader("Authorization") String idToken) {
        if (!authService.verifyToken(idToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized API access");
        }

        String userId = authService.getUserId(idToken);
        // Validate input parameters
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("File is empty. Please upload a valid file.");
        }
        if (classContainer == null || classContainer.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Class container is required.");
        }
        try {
            return analyzeProjectService.analyzeUploadedProject(file, classContainer, userId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Retrieves internal dependencies
    @GetMapping("/intDep")
    public ResponseEntity<String> getInternalDependencies(@RequestHeader("Authorization") String idToken,
                                                          @RequestHeader("project_id") String projectId) {
        if (!authService.verifyToken(idToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized API access");
        }

        if(projectId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Project ID is empty. Cannot retrieve the internal dependency.");
        }

        try {

            // Retrieve internal dependencies after successful authorization
            CompletableFuture<String> response = dependencyRetrievalService.getInternalDependencies(projectId);
            response.join();
            return ResponseEntity.ok(response.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Retrieves external dependencies
    @GetMapping("/extDep")
    public ResponseEntity<String> getExternalDependencies(@RequestHeader("Authorization") String idToken,
                                                          @RequestHeader("project_id") String projectId) {
        if (!authService.verifyToken(idToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized API access");
        }

        if(projectId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Project ID is empty. Cannot retrieve the external dependency.");
        }

        try {
            // Retrieve external dependencies after successful authorization
            CompletableFuture<String> response = dependencyRetrievalService.getExternalDependencies(projectId);
            response.join();
            return ResponseEntity.ok(response.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Retrieves all the class names
    @GetMapping("/classList")
    public ResponseEntity<String> getClasses(@RequestHeader("Authorization") String idToken,
                                             @RequestHeader("project_id") String projectId) {
        if (!authService.verifyToken(idToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized API access");
        }

        if(projectId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Project ID is empty. Cannot retrieve the list.");
        }

        try {
            // Retrieve external dependencies after successful authorization
            CompletableFuture<String> response = dependencyRetrievalService.getClassList(projectId);
            response.join();
            return ResponseEntity.ok(response.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/userProjects")
    public ResponseEntity<String> getUserProjects(@RequestHeader("Authorization") String idToken) {
            if (!authService.verifyToken(idToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized API access");
        }

        String userId = authService.getUserId(idToken);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user");
        }

        try {
            CompletableFuture<List<Map<String, Object>>> projectsFuture = dependencyRetrievalService.getUserProjects(userId);
            List<Map<String, Object>> projects = projectsFuture.get();
            return ResponseEntity.ok(new Gson().toJson(projects));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
