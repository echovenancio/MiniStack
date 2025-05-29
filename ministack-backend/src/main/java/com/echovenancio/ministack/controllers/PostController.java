package com.echovenancio.ministack.controllers;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.echovenancio.ministack.entity.Post;
import com.echovenancio.ministack.entity.Tag;
import com.echovenancio.ministack.entity.User;
import com.echovenancio.ministack.models.CreatePostRequest;
import com.echovenancio.ministack.models.ErrorResponse;
import com.echovenancio.ministack.models.PostDto;
import com.echovenancio.ministack.repository.PostRepository;
import com.echovenancio.ministack.repository.ReplyRepository;
import com.echovenancio.ministack.repository.TagRepository;
import com.echovenancio.ministack.repository.UserRepository;
import com.echovenancio.ministack.utils.Result;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository postRepo;

    private final UserRepository userRepo;

    private final TagRepository tagRepo;

    private final ReplyRepository replyRepo;

    public PostController(PostRepository postRepo, UserRepository userRepo, TagRepository tagRepo,
            ReplyRepository replyRepo) {
        this.postRepo = postRepo;
        this.userRepo = userRepo;
        this.tagRepo = tagRepo;
        this.replyRepo = replyRepo;
    }

    @GetMapping("/")
    public ResponseEntity<Result<Page<PostDto>, ErrorResponse>> getPosts(@RequestParam(required = false) String query,
            @RequestParam(required = false) String tags,
            Pageable pageable) {
        System.out.println(">>> createPost hit");
        if (query != null && query.isBlank()) {
            query = null;
        }
        System.out.println(">>> query: " + query);
        List<String> tagsList = tags != null ? Arrays.asList(tags.split(",")) : null;
        if (tagsList != null && !tagsList.isEmpty()) {
            for (String tag : tagsList) {
                Optional<Tag> foundTag = tagRepo.findByName(tag);
                if (foundTag.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Result.error(
                                    new ErrorResponse("Tag not found", "400")));
                }
            }
        } else {
            tags = null;
        }
        System.out.println(">>> tags: " + tags);
        System.out.println(">>> pageable: " + pageable);
        return ResponseEntity.ok(
                Result.success(postRepo.fullTextSearch(query, tags, pageable)
                        .map(PostDto::new)));
    }

    @Operation(summary = "Create a new post", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<Result<PostDto, ErrorResponse>> createPost(@RequestBody CreatePostRequest newPost,
            Principal principal) {

        String email = principal.getName();
        Optional<User> maybeUser = userRepo.findByEmail(email);

        if (maybeUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(new ErrorResponse("User not found", "401")));
        }

        User user = maybeUser.get();
        Post post = new Post();

        post.setTitle(newPost.getTitle());

        Set<Result<Tag, ErrorResponse>> rawResult = Arrays.stream(newPost.getTags())
                .map(tagName -> tagRepo.findByName(tagName)
                        .<Result<Tag, ErrorResponse>>map(Result::success)
                        .orElse(Result.error(
                                new ErrorResponse("Tag not found: " + tagName, "400"))))
                .collect(Collectors.toSet());

        Optional<ErrorResponse> tagsError = rawResult.stream()
                .filter(Result::isError)
                .map(Result::getError)
                .findFirst();

        if (tagsError.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Result.error(tagsError.get()));
        }

        Set<Tag> tags = rawResult.stream()
                .filter(Result::isSuccess)
                .map(Result::getSuccess)
                .collect(Collectors.toSet());

        post.setBody(newPost.getBody());
        post.setTags(tags);
        post.setUser(user);

        PostDto postDto = new PostDto(postRepo.save(post));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success(postDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<PostDto, ErrorResponse>> getPostById(@PathVariable Long id) {
        Optional<Post> maybePost = postRepo.findById(id);
        if (maybePost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(new ErrorResponse("Post not found", "404")));
        }
        Post post = maybePost.get();
        return ResponseEntity.ok(Result.success(new PostDto(post)));
    }

    @Operation(summary = "Delete a post by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void, ErrorResponse>> deletePost(@PathVariable Long id, Principal principal) {

        Optional<Post> maybePost = postRepo.findById(id);
        if (maybePost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(new ErrorResponse("Post not found", "404")));
        }
        Post post = maybePost.get();

        String email = principal.getName();
        if (!post.getUser().getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.error(new ErrorResponse("You do not have permission to delete this post", "403")));
        }
        post.getReplies().forEach(reply -> {
            reply.setPost(null);
            replyRepo.save(reply);
        });
        postRepo.delete(post);
        return ResponseEntity.ok(Result.success(null));
    }

    @Operation(summary = "Update a post by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ResponseEntity<Result<PostDto, ErrorResponse>> updatePost(@PathVariable Long id,
            @RequestBody CreatePostRequest updatedPost,
            Principal principal) {

        Optional<Post> maybePost = postRepo.findById(id);
        if (maybePost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(new ErrorResponse("Post not found", "404")));
        }
        Post post = maybePost.get();

        String email = principal.getName();

        if (!post.getUser().getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.error(new ErrorResponse("You do not have permission to update this post", "403")));
        }

        post.setTitle(updatedPost.getTitle());
        post.setBody(updatedPost.getBody());

        Set<Result<Tag, ErrorResponse>> rawResult = Arrays.stream(updatedPost.getTags())
                .map(tagName -> tagRepo.findByName(tagName)
                        .<Result<Tag, ErrorResponse>>map(Result::success)
                        .orElse(Result.error(
                                new ErrorResponse("Tag not found: " + tagName, "400"))))
                .collect(Collectors.toSet());

        Optional<ErrorResponse> tagsError = rawResult.stream()
                .filter(Result::isError)
                .map(Result::getError)
                .findFirst();

        if (tagsError.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Result.error(tagsError.get()));
        }

        Set<Tag> tags = rawResult.stream()
                .filter(Result::isSuccess)
                .map(Result::getSuccess)
                .collect(Collectors.toSet());

        post.setTags(tags);
        PostDto postDto = new PostDto(postRepo.save(post));
        return ResponseEntity.ok(Result.success(postDto));
    }
}
