package com.echovenancio.ministack.controllers;

import java.io.Console;
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.echovenancio.ministack.entity.User;
import com.echovenancio.ministack.models.LoginCredentials;
import com.echovenancio.ministack.models.RegisterRequest;
import com.echovenancio.ministack.repository.UserRepository;
import com.echovenancio.ministack.security.JWTUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authManager;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepo, JWTUtil jwtUtil, AuthenticationManager authManager, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
        this.authManager = authManager;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerHandler(@RequestBody RegisterRequest req) {
        try {
            if (!req.getPassword().equals(req.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("error", "Passwords do not match"));
            }
            String encodedPass = passwordEncoder.encode(req.getPassword());
            User user = new User();
            user.setEmail(req.getEmail());
            user.setPassword(encodedPass);
            user.setUsername(req.getUsername());
            user = userRepo.save(user);
            String token = jwtUtil.generateToken(user.getEmail());
            return ResponseEntity.ok(Collections.singletonMap("jwt-token", token));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Internal server error"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginHandler(@RequestBody LoginCredentials body) {
        try {
            UsernamePasswordAuthenticationToken authInputToken = new UsernamePasswordAuthenticationToken(
                    body.getEmail(), body.getPassword());

            authManager.authenticate(authInputToken);

            String token = jwtUtil.generateToken(body.getEmail());

            return ResponseEntity.ok(Collections.singletonMap("jwt-token", token));
        } catch (AuthenticationException authExc) {
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "Invalid credentials"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Internal server error"));
        }
    }

}
