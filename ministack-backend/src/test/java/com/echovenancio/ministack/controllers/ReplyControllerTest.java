package com.echovenancio.ministack.controllers;

import com.echovenancio.ministack.config.TestSecurityConfig;
import com.echovenancio.ministack.entity.Post;
import com.echovenancio.ministack.entity.Reply;
import com.echovenancio.ministack.entity.User;
import com.echovenancio.ministack.models.*; 
import com.echovenancio.ministack.repository.PostRepository;
import com.echovenancio.ministack.repository.ReplyRepository;
import com.echovenancio.ministack.repository.UserRepository;
import com.echovenancio.ministack.security.JWTFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReplyController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        JWTFilter.class } 
))
@AutoConfigureMockMvc(addFilters = true) 
@ContextConfiguration(classes = TestSecurityConfig.class) 
class ReplyControllerTest {

    @Autowired
    private MockMvc mockMvc; 

    @Autowired
    private ObjectMapper objectMapper; 

    @MockitoBean 
    private ReplyRepository replyRepo;

    @MockitoBean
    private PostRepository postRepo;

    @MockitoBean
    private UserRepository userRepo;

    private User mockUser;
    private Post mockPost;
    private Reply mockReply;
    private Reply mockParentReply;
    private Reply mockNestedReply;

    @BeforeEach
    void setUp() {
        Mockito.reset(replyRepo, postRepo, userRepo);

        mockUser = new User(1L, "testuser", "test@example.com", "encodedpassword");
        mockPost = new Post(100L, "Test Post Title", "Test Post Body", mockUser);
        mockReply = new Reply(1L, "This is a top-level reply.", mockUser, mockPost);
        mockParentReply = new Reply(2L, "This is a parent reply.", mockUser, mockPost);
        mockNestedReply = new Reply(3L, "This is a nested reply.", mockPost, mockParentReply, mockUser);
    }

    // --- GET /api/posts/{postId}/replies/ ---
    @Test
    void getReplies_ShouldReturnPageOfReplies() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        List<Reply> replies = Collections.singletonList(mockReply);
        Page<Reply> replyPage = new PageImpl<>(replies, pageable, replies.size());

        when(replyRepo.findByPostIdAndParentReplyIsNull(eq(mockPost.getId()), any(Pageable.class)))
                .thenReturn(replyPage);

        mockMvc.perform(get("/api/posts/{postId}/replies/", mockPost.getId())
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.content[0].id").value(mockReply.getId()))
                .andExpect(jsonPath("$.value.content[0].body").value(mockReply.getBody()))
                .andExpect(jsonPath("$.value.totalElements").value(1));

        verify(replyRepo, times(1)).findByPostIdAndParentReplyIsNull(eq(mockPost.getId()), any(Pageable.class));
    }

    @Test
    void getReplies_ShouldReturnEmptyPage_WhenNoReplies() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reply> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(replyRepo.findByPostIdAndParentReplyIsNull(eq(mockPost.getId()), any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/posts/{postId}/replies/", mockPost.getId())
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.content").isEmpty())
                .andExpect(jsonPath("$.value.totalElements").value(0));

        verify(replyRepo, times(1)).findByPostIdAndParentReplyIsNull(eq(mockPost.getId()), any(Pageable.class));
    }

    // --- GET /api/posts/{postId}/replies/{replyId} ---
    @Test
    void getReply_ShouldReturnReplyDto_WhenFound() throws Exception {
        when(replyRepo.findByIdAndPostId(eq(mockReply.getId()), eq(mockPost.getId())))
                .thenReturn(Optional.of(mockReply));

        mockMvc.perform(get("/api/posts/{postId}/replies/{replyId}", mockPost.getId(), mockReply.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.id").value(mockReply.getId()))
                .andExpect(jsonPath("$.value.body").value(mockReply.getBody()));

        verify(replyRepo, times(1)).findByIdAndPostId(eq(mockReply.getId()), eq(mockPost.getId()));
    }

    @Test
    void getReply_ShouldReturnNotFound_WhenReplyNotFoundForPost() throws Exception {
        when(replyRepo.findByIdAndPostId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/posts/{postId}/replies/{replyId}", mockPost.getId(), 999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Reply not found")); // Adjust based on your ErrorResponse structure

        verify(replyRepo, times(1)).findByIdAndPostId(anyLong(), anyLong());
    }

    // --- GET /api/posts/{postId}/replies/{replyId}/nested ---
    @Test
    void getNestedReplies_ShouldReturnPageOfNestedReplies() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        List<Reply> nestedReplies = Collections.singletonList(mockNestedReply);
        Page<Reply> nestedReplyPage = new PageImpl<>(nestedReplies, pageable, nestedReplies.size());

        when(replyRepo.findByPostIdAndParentReplyId(eq(mockPost.getId()), eq(mockParentReply.getId()), any(Pageable.class)))
                .thenReturn(nestedReplyPage);

        mockMvc.perform(get("/api/posts/{postId}/replies/{replyId}/nested", mockPost.getId(), mockParentReply.getId())
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.content[0].id").value(mockNestedReply.getId()))
                .andExpect(jsonPath("$.value.content[0].body").value(mockNestedReply.getBody()))
                .andExpect(jsonPath("$.value.totalElements").value(1));

        verify(replyRepo, times(1)).findByPostIdAndParentReplyId(eq(mockPost.getId()), eq(mockParentReply.getId()), any(Pageable.class));
    }

    @Test
    void getNestedReplies_ShouldReturnEmptyPage_WhenNoNestedReplies() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reply> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(replyRepo.findByPostIdAndParentReplyId(eq(mockPost.getId()), eq(mockParentReply.getId()), any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/posts/{postId}/replies/{replyId}/nested", mockPost.getId(), mockParentReply.getId())
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.content").isEmpty())
                .andExpect(jsonPath("$.value.totalElements").value(0));

        verify(replyRepo, times(1)).findByPostIdAndParentReplyId(eq(mockPost.getId()), eq(mockParentReply.getId()), any(Pageable.class));
    }

    // --- POST /api/posts/{postId}/replies/ ---
    @Test
    @WithMockUser(username = "test@example.com") // Simulate an authenticated user
    void createReply_ShouldCreateTopLevelReply_WhenValid() throws Exception {
        CreateReplyRequest request = new CreateReplyRequest("New top-level reply body.", null);
        Reply newReply = new Reply(4L, request.getBody(), mockUser, mockPost);

        when(userRepo.findByEmail(eq("test@example.com"))).thenReturn(Optional.of(mockUser));
        when(postRepo.findById(eq(mockPost.getId()))).thenReturn(Optional.of(mockPost));

        when(replyRepo.save(any(Reply.class))).thenReturn(newReply);

        mockMvc.perform(post("/api/posts/{postId}/replies/", mockPost.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.value.id").value(newReply.getId()))
                .andExpect(jsonPath("$.value.body").value(newReply.getBody()));

        verify(userRepo, times(1)).findByEmail(eq("test@example.com"));
        verify(postRepo, times(1)).findById(eq(mockPost.getId()));
        verify(replyRepo, times(1)).save(any(Reply.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createReply_ShouldCreateNestedReply_WhenParentExists() throws Exception {
        CreateReplyRequest request = new CreateReplyRequest("New nested reply body.", mockParentReply.getId());
        Reply newNestedReply = new Reply(5L, request.getBody(), mockPost, mockParentReply, mockUser);

        when(userRepo.findByEmail(eq("test@example.com"))).thenReturn(Optional.of(mockUser));
        when(postRepo.findById(eq(mockPost.getId()))).thenReturn(Optional.of(mockPost));
        when(replyRepo.findById(eq(mockParentReply.getId()))).thenReturn(Optional.of(mockParentReply)); 
        when(replyRepo.save(any(Reply.class))).thenReturn(newNestedReply);

        mockMvc.perform(post("/api/posts/{postId}/replies/", mockPost.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.value.id").value(newNestedReply.getId()))
                .andExpect(jsonPath("$.value.parentReplyId").value(mockParentReply.getId()));

        verify(userRepo, times(1)).findByEmail(eq("test@example.com"));
        verify(postRepo, times(1)).findById(eq(mockPost.getId()));
        verify(replyRepo, times(1)).findById(eq(mockParentReply.getId()));
        verify(replyRepo, times(1)).save(any(Reply.class));
    }

    @Test
    void createReply_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        CreateReplyRequest request = new CreateReplyRequest("Some reply", null);

        mockMvc.perform(post("/api/posts/{postId}/replies/", mockPost.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()); // No @WithMockUser
        // No repository interactions should happen
        verifyNoInteractions(userRepo, postRepo, replyRepo);
    }

    @Test
    @WithMockUser(username = "nonexistent@example.com")
    void createReply_ShouldReturnUnauthorized_WhenUserNotFound() throws Exception {
        CreateReplyRequest request = new CreateReplyRequest("Some reply", null);

        when(userRepo.findByEmail(eq("nonexistent@example.com"))).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/posts/{postId}/replies/", mockPost.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.message").value("User not found"));

        verify(userRepo, times(1)).findByEmail(eq("nonexistent@example.com"));
        verifyNoMoreInteractions(userRepo); 
        verifyNoInteractions(postRepo, replyRepo);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createReply_ShouldReturnNotFound_WhenPostNotFound() throws Exception {
        CreateReplyRequest request = new CreateReplyRequest("Some reply", null);

        when(userRepo.findByEmail(eq("test@example.com"))).thenReturn(Optional.of(mockUser));
        when(postRepo.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/posts/{postId}/replies/", 999L) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Post not found"));

        verify(userRepo, times(1)).findByEmail(eq("test@example.com"));
        verify(postRepo, times(1)).findById(anyLong());
        verifyNoInteractions(replyRepo); 
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createReply_ShouldReturnBadRequest_WhenParentReplyNotFound() throws Exception {
        CreateReplyRequest request = new CreateReplyRequest("Some reply", 999L); 

        when(userRepo.findByEmail(eq("test@example.com"))).thenReturn(Optional.of(mockUser));
        when(replyRepo.findById(anyLong())).thenReturn(Optional.empty()); 

        mockMvc.perform(post("/api/posts/{postId}/replies/", mockPost.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Parent reply not found"));

        verify(userRepo, times(1)).findByEmail(eq("test@example.com"));
        verify(replyRepo, times(1)).findById(anyLong());
        verifyNoInteractions(postRepo); 
    }


    // --- DELETE /api/posts/{postId}/replies/{replyId} ---
    @Test
    @WithMockUser(username = "test@example.com", roles = "USER") 
    void deleteReply_ShouldReturnOk_WhenUserIsOwner() throws Exception {
        when(replyRepo.findById(eq(mockReply.getId()))).thenReturn(Optional.of(mockReply));
        doNothing().when(replyRepo).delete(any(Reply.class));

        mockMvc.perform(delete("/api/posts/{postId}/replies/{replyId}", mockPost.getId(), mockReply.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(replyRepo, times(1)).findById(eq(mockReply.getId()));
        verify(replyRepo, times(1)).delete(eq(mockReply));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void deleteReply_ShouldReturnNotFound_WhenReplyDoesNotExist() throws Exception {
        when(replyRepo.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/posts/{postId}/replies/{replyId}", mockPost.getId(), 999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Reply not found"));

        verify(replyRepo, times(1)).findById(anyLong());
        verify(replyRepo, never()).delete(any(Reply.class));
    }

    @Test
    @WithMockUser(username = "other@example.com", roles = "USER") 
    void deleteReply_ShouldReturnForbidden_WhenUserIsNotOwner() throws Exception {
        when(replyRepo.findById(eq(mockReply.getId()))).thenReturn(Optional.of(mockReply));

        mockMvc.perform(delete("/api/posts/{postId}/replies/{replyId}", mockPost.getId(), mockReply.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("You do not have permission to delete this reply"));

        verify(replyRepo, times(1)).findById(eq(mockReply.getId()));
        verify(replyRepo, never()).delete(any(Reply.class)); 
    }

    @Test
    void deleteReply_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/posts/{postId}/replies/{replyId}", mockPost.getId(), mockReply.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(replyRepo);
    }


    // --- PUT /api/posts/{postId}/replies/{replyId} ---
    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void updateReply_ShouldReturnOk_WhenUserIsOwner() throws Exception {
        UpdateReplyRequest request = new UpdateReplyRequest("Updated reply body.");
        Reply updatedReply = new Reply(mockReply.getId(), request.getBody(), mockReply.getPost(), mockReply.getParentReply(), mockReply.getUser());

        when(replyRepo.findById(eq(mockReply.getId()))).thenReturn(Optional.of(mockReply));
        when(replyRepo.save(any(Reply.class))).thenReturn(updatedReply);

        mockMvc.perform(put("/api/posts/{postId}/replies/{replyId}", mockPost.getId(), mockReply.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.id").value(updatedReply.getId()))
                .andExpect(jsonPath("$.value.body").value(updatedReply.getBody()));

        verify(replyRepo, times(1)).findById(eq(mockReply.getId()));
        verify(replyRepo, times(1)).save(any(Reply.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void updateReply_ShouldReturnNotFound_WhenReplyDoesNotExist() throws Exception {
        UpdateReplyRequest request = new UpdateReplyRequest("Updated reply body.");
        when(replyRepo.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/posts/{postId}/replies/{replyId}", mockPost.getId(), 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Reply not found"));

        verify(replyRepo, times(1)).findById(anyLong());
        verify(replyRepo, never()).save(any(Reply.class));
    }

    @Test
    @WithMockUser(username = "other@example.com", roles = "USER")
    void updateReply_ShouldReturnForbidden_WhenUserIsNotOwner() throws Exception {
        UpdateReplyRequest request = new UpdateReplyRequest("Updated reply body.");
        when(replyRepo.findById(eq(mockReply.getId()))).thenReturn(Optional.of(mockReply));

        mockMvc.perform(put("/api/posts/{postId}/replies/{replyId}", mockPost.getId(), mockReply.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("You do not have permission to update this reply"));

        verify(replyRepo, times(1)).findById(eq(mockReply.getId()));
        verify(replyRepo, never()).save(any(Reply.class));
    }

    @Test
    void updateReply_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        UpdateReplyRequest request = new UpdateReplyRequest("Updated reply body.");
        mockMvc.perform(put("/api/posts/{postId}/replies/{replyId}", mockPost.getId(), mockReply.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(replyRepo);
    }
}
