package com.g8.controller;

import com.g8.service.AuthService;
import com.g8.service.DependencyRetrievalService;
import com.g8.service.AnalyzeProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/initialize")
@CrossOrigin(origins = "*")
public class UploadController {

    private final AnalyzeProjectService analyzeProjectService;
    private final AuthService authService;

    @Autowired
    public UploadController(AnalyzeProjectService analyzeProjectService, AuthService authService) {
        this.analyzeProjectService = analyzeProjectService;
        this.authService = authService;
    }

    // Saves the uploaded file and analyzes it
    @PostMapping("/upload")
    public ResponseEntity<String> uploadProject(@RequestParam("file") MultipartFile file,
                                                @RequestParam("classContainer") String classContainer,
                                                @RequestHeader("Authorization") String idToken) {
        if (!authService.verifyToken(idToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized API access");
        }
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
            return analyzeProjectService.analyzeUploadedProject(file, classContainer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to analyze project");
        }
    }

    // Retrieves internal dependencies
    @GetMapping("/intDep")
    public ResponseEntity<String> getInternalDependencies(@RequestHeader("Authorization") String idToken,
                                                          @RequestHeader("project_id") String projectId) {
        if (!authService.verifyToken(idToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized API access");
        }
        try {
            // Retrieve internal dependencies after successful authorization
            String response = DependencyRetrievalService.getInternalDependencies(projectId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve internal dependencies");
        }
    }

    // Retrieves external dependencies
    @GetMapping("/extDep")
    public ResponseEntity<String> getExternalDependencies(@RequestHeader("Authorization") String idToken,
                                                          @RequestHeader("project_id") String projectId) {
        if (!authService.verifyToken(idToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized API access");
        }
        try {
            // Retrieve external dependencies after successful authorization
            String response = DependencyRetrievalService.getExternalDependencies(projectId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve external dependencies");
        }
    }

    // Retrieves all the class names
    @GetMapping("/classList")
    public ResponseEntity<String> getClasses(@RequestHeader("Authorization") String idToken,
                                             @RequestHeader("project_id") String projectId) {
        if (!authService.verifyToken(idToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized API access");
        }
        try {
            // Retrieve external dependencies after successful authorization
            String response = DependencyRetrievalService.getClassList(projectId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve external dependencies");
        }
    }
}
