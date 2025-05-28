package com.echovenancio.ministack.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.echovenancio.ministack.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query(value = """
            SELECT DISTINCT p.*
            FROM post p
            LEFT JOIN post_tags pt ON p.id = pt.post_id
            LEFT JOIN tag t ON pt.tag_id = t.id
            WHERE (:tags IS NULL OR t.name = ANY (string_to_array(:tags, ',')))
            AND (
                :query IS NULL OR
                to_tsvector('simple', p.title || ' ' || p.body) @@ plainto_tsquery(:query)
            )
            """, countQuery = """
            SELECT COUNT(DISTINCT p.id)
            FROM post p
            LEFT JOIN post_tags pt ON p.id = pt.post_id
            LEFT JOIN tag t ON pt.tag_id = t.id
            WHERE (:tags IS NULL OR t.name = ANY (string_to_array(:tags, ',')))
            AND (
                :query IS NULL OR
                to_tsvector('simple', p.title || ' ' || p.body) @@ plainto_tsquery(:query)
            )
            """, nativeQuery = true)
    Page<Post> fullTextSearch(
            @Param("query") String query,
            @Param("tags") String tags,
            Pageable pageable);

}
