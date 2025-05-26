package com.echovenancio.ministack.models;

import com.echovenancio.ministack.entity.Reply;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReplyDto {
    private Long id;
    private String body;
    private Long postId;
    private Long parentReplyId;
    private Long userId;
    private String username;
    private String createdAt;

    public ReplyDto(Reply reply) {
        this.id = reply.getId();
        this.body = reply.getBody();
        this.postId = reply.getPost() != null ? reply.getPost().getId() : null;
        this.parentReplyId = reply.getParentReply() != null ? reply.getParentReply().getId() : null;
        this.userId = reply.getUser() != null ? reply.getUser().getId() : null;
        this.username = reply.getUser() != null ? reply.getUser().getUsername() : null;
        this.createdAt = reply.getCreatedAt() != null ? reply.getCreatedAt().toString() : null;
    }
}
