package com.echovenancio.ministack.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.echovenancio.ministack.entity.User;

public interface UserRepository extends JpaRepository<com.echovenancio.ministack.entity.User, Long> {
    public Optional<User> findByUsername(String username);
    public Optional<User> findByEmail(String email);
}
