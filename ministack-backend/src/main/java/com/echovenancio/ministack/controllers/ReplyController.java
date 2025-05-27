package com.echovenancio.ministack.controllers;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.echovenancio.ministack.models.ReplyDto;
import com.echovenancio.ministack.repository.PostRepository;
import com.echovenancio.ministack.repository.ReplyRepository;
import com.echovenancio.ministack.repository.UserRepository;

@RestController
@RequestMapping("/api/posts/{postId}/replies")
public class ReplyController {

    @Autowired
    private ReplyRepository replyRepo;

    @Autowired
    private PostRepository postRepo; 

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/{replyId}")
    public ResponseEntity<ReplyDto> getReply(@PathVariable Long postId, @PathVariable Long replyId) {
        if (postId == null || replyId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post ID and Reply ID must not be null"); 
        }
        Reply reply = replyRepo.findById(replyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reply not found"));
        return ResponseEntity.ok(new ReplyDto(reply));
    }

    @PostMapping("/")
    public ResponseEntity<ReplyDto> createReply(@PathVariable Long postId, @RequestBody CreateReplyRequest req) {
        if (postId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post ID and reply body must not be null or empty");
        }
        User user = userRepo.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Reply reply = new Reply();

        if (req.getParentReplyId() != null) {
            Reply parentReply = replyRepo.findById(req.getParentReplyId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent reply not found"));
            reply.setParentReply(parentReply);
        }
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        reply.setPost(post);
        reply.setBody(req.getBody());
        reply.setUser(user); 
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ReplyDto(replyRepo.save(reply)));
    }

    @DeleteMapping("/{replyId}")
    public ResponseEntity<Void> deleteReply(@PathVariable Long postId, @PathVariable Long replyId) {
        if (postId == null || replyId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post ID and Reply ID must not be null");
        }
        Reply reply = replyRepo.findById(replyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reply not found"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        if (!reply.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete this reply");
        }
        replyRepo.delete(reply);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{replyId}")
    public ResponseEntity<ReplyDto> updateReply(@PathVariable Long postId, @PathVariable Long replyId, @RequestBody ReplyDto replyDto) {
        if (postId == null || replyId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post ID and Reply ID must not be null");
        }
        Reply reply = replyRepo.findById(replyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reply not found"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        if (!reply.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to update this reply");
        }
        reply.setBody(replyDto.getBody());
        replyRepo.save(reply);
        return ResponseEntity.ok(new ReplyDto(reply));
    }
}
