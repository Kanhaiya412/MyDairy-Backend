package com.MyFarmerApp.MyFarmer.controller;

import com.MyFarmerApp.MyFarmer.dto.LoginRequest;
import com.MyFarmerApp.MyFarmer.dto.RegisterRequest;
import com.MyFarmerApp.MyFarmer.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        String message = authService.register(request);
        boolean success = !message.contains("already registered");

        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", message
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Map<String, Object> result = authService.login(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getuser")
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> result = authService.getUserFromToken(authHeader);
        return ResponseEntity.ok(result);
    }

}
