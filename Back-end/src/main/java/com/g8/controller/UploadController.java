package com.g8.controller;

import com.g8.service.DependencyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/initialize")
public class UploadController {

    private final DependencyHandler dependencyHandler;

    @Autowired
    public UploadController(DependencyHandler dependencyHandler) {
        this.dependencyHandler = dependencyHandler;
    }

    // Saves the uploaded file and analyzes it
    @PostMapping("/upload")
    public ResponseEntity<String> uploadProject(@RequestParam("file") MultipartFile file, @RequestParam("classContainer") String classContainer) {
        
        try {
            return dependencyHandler.analyzeUploadedProject(file, classContainer);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to analyze project");
        }
    }

    // Retrieves internal dependencies
    @GetMapping("/intDep")
    public ResponseEntity<String> getInternalDependencies() {
        String response = dependencyHandler.getInternalDependencies();
        return ResponseEntity.ok(response);
    }

    // Retrieves external dependencies
    @GetMapping("/extDep")
    public ResponseEntity<String> getExternalDependencies() {
        String response = dependencyHandler.getExternalDependencies();
        return ResponseEntity.ok(response);
    }

    // Retrieves all the class names
    @GetMapping("/classList")
    public ResponseEntity<String> getClasses() {
        String response = dependencyHandler.getClassList();
        return ResponseEntity.ok(response);
    }
}
