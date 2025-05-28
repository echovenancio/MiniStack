package com.echovenancio.ministack.controllers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.echovenancio.ministack.entity.Post;
import com.echovenancio.ministack.entity.Tag;
import com.echovenancio.ministack.entity.User;
import com.echovenancio.ministack.models.CreatePostRequest;
import com.echovenancio.ministack.models.PostDto;
import com.echovenancio.ministack.models.ReplyDto;
import com.echovenancio.ministack.repository.PostRepository;
import com.echovenancio.ministack.repository.ReplyRepository;
import com.echovenancio.ministack.repository.TagRepository;
import com.echovenancio.ministack.repository.UserRepository;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostRepository postRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TagRepository tagRepo;

    @Autowired
    private ReplyRepository replyRepo;

    @GetMapping("/")
    public Page<PostDto> getPosts(@RequestParam(required = false) String query,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Pageable pageable) {
        System.out.println(">>> createPost hit");
        return postRepo.fullTextSearch(query, tag, pageable)
                .map(PostDto::new);
    }

    @GetMapping("/{postId}/replies")
    public Page<ReplyDto> getReplies(@PathVariable Long postId, Pageable pageable) {
        if (postId == null) {
            return Page.empty();
        }
        Page<ReplyDto> replies = replyRepo.findByPostId(postId, pageable)
                .map(ReplyDto::new);
        return replies;
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(@RequestBody CreatePostRequest newPost) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
        Post post = new Post();
        post.setTitle(newPost.getTitle());
        Set<Tag> tags = Arrays.stream(newPost.getTags())
                .map(tagName -> tagRepo.findByName(tagName)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Tag not found: " + tagName)))
                .collect(Collectors.toSet());
        post.setBody(newPost.getBody());
        post.setTags(tags);
        post.setUser(user);
        PostDto postDto = new PostDto(postRepo.save(post));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postDto);
    }

    @GetMapping("/{id}")
    public Post getPostById(@PathVariable Long id) {
        return postRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        Post post = postRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        if (!post.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete this post");
        }
        postRepo.delete(post);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable Long id, @RequestBody CreatePostRequest updatedPost) {
        Post post = postRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        if (!post.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to update this post");
        }
        post.setTitle(updatedPost.getTitle());
        post.setBody(updatedPost.getBody());
        Set<Tag> tags = Arrays.stream(updatedPost.getTags())
                .map(tagName -> tagRepo.findByName(tagName)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Tag not found: " + tagName)))
                .collect(Collectors.toSet());
        post.setTags(tags);
        PostDto postDto = new PostDto(postRepo.save(post));
        return ResponseEntity.ok(postDto);
    }
}
