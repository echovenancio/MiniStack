package com.echovenancio.ministack.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<com.echovenancio.ministack.entity.Tag, Long> {
    Optional<com.echovenancio.ministack.entity.Tag> findByName(String name);
}
