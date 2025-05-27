package com.echovenancio.ministack.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.echovenancio.ministack.entity.Reply;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    Page<Reply> findByPostId(Long postId, Pageable pageable);
    Page<Reply> findByParentReplyId(Long parentReplyId, Pageable pageable);
}
