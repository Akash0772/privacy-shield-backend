package com.privacyshield.privacy_shield.service;

import com.privacyshield.privacy_shield.dto.LoginRequest;
import com.privacyshield.privacy_shield.dto.RegisterRequest;
import com.privacyshield.privacy_shield.entity.User;
import com.privacyshield.privacy_shield.repository.UserRepository;
import com.privacyshield.privacy_shield.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor   // Lombok — constructor auto banata hai
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // --- REGISTER ---
    public Map<String, String> register(RegisterRequest req) {

        // Email already hai?
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException(
                    "Email already registered hai!"
            );
        }

        // Naya user banao
        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(
                passwordEncoder.encode(req.getPassword()) // Encrypt!
        );
        user.setCaFirmName(req.getCaFirmName());

        userRepository.save(user); // Database mein save karo

        Map<String, String> response = new HashMap<>();
        response.put("message", "Registration successful!");
        response.put("email", user.getEmail());
        return response;
    }

    // --- LOGIN ---
    public Map<String, String> login(LoginRequest req) {

        // User dhundho
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Email nahi mila!")
                );

        // Password check karo
        if (!passwordEncoder.matches(
                req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password galat hai!");
        }

        // JWT token banao
        String token = jwtUtil.generateToken(user.getEmail());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("email", user.getEmail());
        response.put("caFirmName", user.getCaFirmName());
        response.put("plan", user.getPlan());
        return response;
    }
}
