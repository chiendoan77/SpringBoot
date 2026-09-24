package com.example.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.sercurity.JwtService;

@RestController 
@RequestMapping("/auth")
public class AuthController {
    private final JwtService jwtService;
    public AuthController(JwtService jwtService){
        this.jwtService = jwtService;
    }
    @PostMapping
    public Map<String, String> login(
        @RequestParam String userName, @RequestParam String password
    ){
        Map<String,String> result = new HashMap<>();
        if (userName.equals("admin") && password.equals("123")){
            String token = jwtService.generateToken(userName);
            result.put("message", "login success");
            result.put("token", token);
        }else{
            result.put("message", "Invalid usernam or password");
        }
        return result;
    }
}
