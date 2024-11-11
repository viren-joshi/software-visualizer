package com.g8.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g8.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    public AuthController(AuthService authService) {
        // Constructor
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestParam String email, @RequestParam String password, @RequestParam String name) {
        // Skeleton for signUp endpoint
        return null;
    }

    @PostMapping("/verfyToken")
    public ResponseEntity<String> verifyToken(@RequestHeader("Authorization") String idToken) {
        // Skeleton for verifyToken endpoint
        return null;
    }
}
