package com.echovenancio.ministack.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.echovenancio.ministack.entity.User;
import com.echovenancio.ministack.repository.UserRepository;

@RestController
@RequestMapping("/api/user")
public class UserController {

   private final UserRepository userRepo;

    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/info")
    public ResponseEntity<User> getUserDetails(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        String email = principal.getName();
        return userRepo.findByEmail(email)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
