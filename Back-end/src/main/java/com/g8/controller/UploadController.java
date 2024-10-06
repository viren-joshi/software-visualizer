package com.g8.controller;

import com.g8.service.DependecyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/initialize")
public class UploadController {

    private DependecyHandler dependecyHandler;

    @Autowired
    public UploadController(DependecyHandler dependecyHandler) {
        this.dependecyHandler = dependecyHandler;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadProject(@RequestParam("file") MultipartFile file, @RequestParam("classContainer") String classContainer) {
        
        try {
            String result = dependecyHandler.analyzeUploadedProject(file, classContainer);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to analyze project: " + e.getMessage());
        }
    }

}
