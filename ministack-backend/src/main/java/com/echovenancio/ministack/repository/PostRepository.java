package com.echovenancio.ministack.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.echovenancio.ministack.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("""
                SELECT p FROM Post p
                JOIN p.tags t
                WHERE (:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))
                AND (:tag IS NULL OR t.name = :tag)
                AND (:body IS NULL OR LOWER(p.body) LIKE LOWER(CONCAT('%', :body, '%')))
            """)
    Page<Post> searchPosts(@Param("title") String title,
            @Param("tag") String tag,
            @Param("body") String body, 
            Pageable pageable);
}
