package com.echovenancio.ministack.controllers;

import java.security.Principal;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.echovenancio.ministack.entity.Post;
import com.echovenancio.ministack.entity.Reply;
import com.echovenancio.ministack.entity.User;
import com.echovenancio.ministack.models.CreateReplyRequest;
import com.echovenancio.ministack.models.ErrorResponse;
import com.echovenancio.ministack.models.ReplyDto;
import com.echovenancio.ministack.models.UpdateReplyRequest;
import com.echovenancio.ministack.repository.PostRepository;
import com.echovenancio.ministack.repository.ReplyRepository;
import com.echovenancio.ministack.repository.UserRepository;
import com.echovenancio.ministack.utils.Result;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/posts/{postId}/replies")
public class ReplyController {

    @Autowired
    private ReplyRepository replyRepo;

    @Autowired
    private PostRepository postRepo;

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/")
    public ResponseEntity<Result<Page<ReplyDto>, ErrorResponse>> getReplies(@PathVariable Long postId,
            Pageable pageable) {
        Page<ReplyDto> replies = replyRepo.findByPostIdAndParentReplyIsNull(postId, pageable)
                .map(ReplyDto::new);
        return ResponseEntity.ok(Result.success(replies));
    }

    @GetMapping("/{replyId}")
    public ResponseEntity<Result<ReplyDto, ErrorResponse>> getReply(@PathVariable Long postId, @PathVariable Long replyId) {
        Optional<Reply> maybeReply = replyRepo.findByIdAndPostId(replyId, postId);
        if (maybeReply.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(new ErrorResponse("Reply not found", "404")));
        }
        Reply reply = maybeReply.get();
        return ResponseEntity.ok(Result.success(new ReplyDto(reply)));
    }

    @GetMapping("/{replyId}/nested")
    public ResponseEntity<Result<Page<ReplyDto>, ErrorResponse>> getNestedReplies(@PathVariable Long postId, @PathVariable Long replyId,
            Pageable pageable) {
        Page<ReplyDto> nestedReplies = replyRepo.findByPostIdAndParentReplyId(postId, replyId, pageable)
                .map(ReplyDto::new);
        return ResponseEntity.ok(Result.success(nestedReplies));
    }

    @Operation(summary = "Create a new reply to a post", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/")
    public ResponseEntity<Result<ReplyDto, ErrorResponse>> createReply(@PathVariable Long postId, @RequestBody CreateReplyRequest req,
            Principal principal) {

        String email = principal.getName();
        Optional<User> maybeUser = userRepo.findByEmail(email);
        if (maybeUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(new ErrorResponse("User not found", "401")));
        }
        User user = maybeUser.get();

        Reply reply = new Reply();

        if (req.getParentReplyId() != null) {
            Optional<Reply> maybeParentReply = replyRepo.findById(req.getParentReplyId());
            if (maybeParentReply.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(new ErrorResponse("Parent reply not found", "400")));
            }
            reply.setParentReply(maybeParentReply.get());
        }

        Optional<Post> maybePost = postRepo.findById(postId);
        if (maybePost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(new ErrorResponse("Post not found", "404")));
        }
        Post post = maybePost.get();

        reply.setPost(post);
        reply.setBody(req.getBody());
        reply.setUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success(new ReplyDto(replyRepo.save(reply))));
    }

    @Operation(summary = "Delete a reply", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{replyId}")
    public ResponseEntity<Result<Void, ErrorResponse>> deleteReply(@PathVariable Long postId, @PathVariable Long replyId,
            Principal principal) {
        Optional<Reply> maybeReply = replyRepo.findById(replyId);
        if (maybeReply.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(new ErrorResponse("Reply not found", "404")));
        }
        Reply reply = maybeReply.get();

        String email = principal.getName();
        if (!reply.getUser().getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.error(new ErrorResponse("You do not have permission to delete this reply", "403")));
        }

        replyRepo.delete(reply);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update a reply", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{replyId}")
    public ResponseEntity<Result<ReplyDto, ErrorResponse>> updateReply(@PathVariable Long postId, @PathVariable Long replyId,
            @RequestBody UpdateReplyRequest updateReplyRequest, Principal principal) {
        Optional<Reply> maybeReply = replyRepo.findById(replyId);
        if (maybeReply.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(new ErrorResponse("Reply not found", "404")));
        }
        Reply reply = maybeReply.get();

        String email = principal.getName();
        if (!reply.getUser().getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.error(new ErrorResponse("You do not have permission to update this reply", "403")));
        }

        reply.setBody(updateReplyRequest.getBody());
        replyRepo.save(reply);
        return ResponseEntity.ok(Result.success(new ReplyDto(reply)));
    }
}
