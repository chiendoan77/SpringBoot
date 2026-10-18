package com.example.auth.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth.security.JwtService;

@RestController 
@RequestMapping("/auth")
public class AuthController {
    private final JwtService jwtService;
    private final String configuredUsername;
    private final String configuredPassword;

    public AuthController(
            JwtService jwtService,
            @Value("${auth.username:admin}") String configuredUsername,
            @Value("${auth.password:123}") String configuredPassword) {
        this.jwtService = jwtService;
        this.configuredUsername = configuredUsername;
        this.configuredPassword = configuredPassword;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> login(
            @RequestParam String userName,
            @RequestParam String password) {
        Map<String, String> result = new HashMap<>();
        if (configuredUsername.equals(userName) && configuredPassword.equals(password)) {
            String token = jwtService.generateToken(userName);
            result.put("message", "login success");
            result.put("token", token);
            return ResponseEntity.ok(result);
        }

        result.put("message", "Invalid username or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }
}
