package com.MyFarmerApp.MyFarmer.service;

import com.MyFarmerApp.MyFarmer.config.JwtUtil;
import com.MyFarmerApp.MyFarmer.dto.LoginRequest;
import com.MyFarmerApp.MyFarmer.dto.RegisterRequest;
import com.MyFarmerApp.MyFarmer.entity.User;
import com.MyFarmerApp.MyFarmer.enums.Role;
import com.MyFarmerApp.MyFarmer.repository.UserRepository;
import com.MyFarmerApp.MyFarmer.util.UserEventPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private KafkaProducerService kafkaProducer;

    // ✅ Register new user
    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            kafkaProducer.send(UserEventPayload.json(
                    "REGISTER_FAILED", request.getEmail(), "UNKNOWN", false, "email_exists"));
            return "Email already exists!";
        }

        Role userRole;
        try {
            userRole = Role.valueOf(request.getRole().toString().toUpperCase());
        } catch (Exception e) {
            userRole = Role.FARMER;
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .build();

        userRepository.save(user);

        kafkaProducer.send(UserEventPayload.json(
                "REGISTER_SUCCESS", user.getEmail(), user.getRole().name(), true, "registered"));

        return "User registered successfully!";
    }

    // ✅ Login existing user
    public Map<String, Object> login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            kafkaProducer.send(UserEventPayload.json(
                    "LOGIN_FAILED", request.getUsername(), user.getRole().name(), false, "bad_password"));
            throw new RuntimeException("Invalid credentials!");
        }

        // ✅ Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername());

        // ✅ Build clean response map
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());
        response.put("userId", user.getId()); // ✅ add userId for frontend

        kafkaProducer.send(UserEventPayload.json(
                "LOGIN_SUCCESS", user.getUsername(), user.getRole().name(), true, "logged_in"));

        return response;
    }

    // ✅ Get user info by username
    public Map<String, Object> getUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());
        response.put("userId", user.getId()); // include userId for completeness
        return response;
    }

    // ✅ Get user info from JWT token
    public Map<String, Object> getUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid token!");
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        return getUser(username); // reuse
    }

    // ✅ Get User by ID (for Expense Management)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }
}
