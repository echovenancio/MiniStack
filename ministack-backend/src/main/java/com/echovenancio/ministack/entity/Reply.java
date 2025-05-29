package com.echovenancio.ministack.entity;

import java.time.LocalDateTime;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@ToString
public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_reply_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Reply parentReply;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentReply")
    private Set<Reply> childReplies;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User user;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Reply() {
    }

    public Reply(String body, Post post, Reply parentReply, User user) {
        this.body = body;
        this.post = post;
        this.parentReply = parentReply;
        this.user = user;
    }

    public Reply(Long id, String body, User user, Post post) {
        this.id = id;
        this.body = body;
        this.post = post;
        this.user = user;
    }

    public Reply(Long id, String body, Post post, Reply parentReply, User user) {
        this.id = id;
        this.body = body;
        this.post = post;
        this.parentReply = parentReply;
        this.user = user;
    }

    public Reply(Long id, String body, Post post, Reply parentReply, Set<Reply> childReplies, User user) {
        this.id = id;
        this.body = body;
        this.post = post;
        this.parentReply = parentReply;
        this.childReplies = childReplies;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Reply getParentReply() {
        return parentReply;
    }

    public void setParentReply(Reply parentReply) {
        this.parentReply = parentReply;
    }

    public Set<Reply> getChildReplies() {
        return childReplies;
    }

    public void setChildReplies(Set<Reply> childReplies) {
        this.childReplies = childReplies;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
