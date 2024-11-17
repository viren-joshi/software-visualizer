package com.g8.controller;

import com.g8.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestParam String email, @RequestParam String password, @RequestParam String name) {
        return authService.signUp(email, password, name);   
    }

    @PostMapping("/verifyToken")
    public ResponseEntity<String> verifyToken(@RequestHeader("Authorization") String idToken) {
        if(authService.verifyToken(idToken)) {
            return ResponseEntity.ok("{isVerified: true}");
        } else {
            return ResponseEntity.status(401).body("{isVerified: false}");
        }
    }

}