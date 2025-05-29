package com.echovenancio.ministack.controllers;

import com.echovenancio.ministack.config.TestSecurityConfig;
import com.echovenancio.ministack.entity.Post;
import com.echovenancio.ministack.entity.Reply;
import com.echovenancio.ministack.entity.Tag;
import com.echovenancio.ministack.entity.User;
import com.echovenancio.ministack.models.CreatePostRequest;
import com.echovenancio.ministack.repository.PostRepository;
import com.echovenancio.ministack.repository.ReplyRepository;
import com.echovenancio.ministack.repository.TagRepository;
import com.echovenancio.ministack.repository.UserRepository;
import com.echovenancio.ministack.security.JWTFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; 
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PostController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        JWTFilter.class }
))
@AutoConfigureMockMvc(addFilters = true)
@ContextConfiguration(classes = TestSecurityConfig.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostRepository postRepo;

    @MockitoBean
    private UserRepository userRepo;

    @MockitoBean
    private TagRepository tagRepo;

    @MockitoBean
    private ReplyRepository replyRepo;

    // Common mock data
    private User mockUser;
    private Tag mockTag1;
    private Tag mockTag2;
    private Post mockPost;
    private Reply mockReply;

    @BeforeEach
    void setUp() {
        mockUser = new User(1L, "testuser", "user@example.com", "encoded_password");
        mockTag1 = new Tag(10L, "java");
        mockTag2 = new Tag(11L, "spring");
        mockPost = new Post(100L, "Test Post Title", "Test Post Body", mockUser,
                new HashSet<>(Arrays.asList(mockTag1, mockTag2)), new HashSet<>());
        mockReply = new Reply(200L, "Great post!", mockUser, mockPost);
        mockPost.getReplies().add(mockReply);
    }

    @Test
    void testGetPosts_Success_NoParams() throws Exception {
        List<Post> posts = Collections.singletonList(mockPost);
        Pageable pageable = PageRequest.of(0, 20); 
        when(postRepo.fullTextSearch(eq(null), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(posts, pageable, posts.size()));

        mockMvc.perform(get("/api/posts/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.content[0].id").value(String.valueOf(mockPost.getId())))
                .andExpect(jsonPath("$.value.content[0].title").value(mockPost.getTitle()))
                .andExpect(jsonPath("$.value.content[0].authorUsername").value(mockUser.getUsername()))
                .andExpect(jsonPath("$.error").doesNotExist()); 
    }

    @Test
    void testGetPosts_Success_WithQueryAndTags() throws Exception {
        String query = "test";
        String tags = "java,spring";
        List<Post> posts = Collections.singletonList(mockPost);
        Pageable pageable = PageRequest.of(0, 10);

        when(tagRepo.findByName("java")).thenReturn(Optional.of(mockTag1));
        when(tagRepo.findByName("spring")).thenReturn(Optional.of(mockTag2));
        when(postRepo.fullTextSearch(eq(query), eq(tags), eq(pageable)))
                .thenReturn(new PageImpl<>(posts, pageable, posts.size()));

        mockMvc.perform(get("/api/posts/")
                .param("query", query)
                .param("tags", tags)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.content[0].title").value(mockPost.getTitle()))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void testGetPosts_Error_TagNotFound() throws Exception {
        String tags = "java,unknown";
        when(tagRepo.findByName("java")).thenReturn(Optional.of(mockTag1));
        when(tagRepo.findByName("unknown")).thenReturn(Optional.empty()); 

        mockMvc.perform(get("/api/posts/")
                .param("tags", tags)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.value").doesNotExist()) 
                .andExpect(jsonPath("$.error.message").value("Tag not found")) 
                .andExpect(jsonPath("$.error.code").value("400"));
    }

    @Test
    void testGetPosts_Success_EmptyQueryStringHandledAsNull() throws Exception {
        List<Post> posts = Collections.singletonList(mockPost);
        Pageable pageable = PageRequest.of(0, 20);

        when(postRepo.fullTextSearch(ArgumentMatchers.isNull(), ArgumentMatchers.isNull(),
                ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(posts, pageable, posts.size()));

        mockMvc.perform(get("/api/posts/")
                .param("query", "") 
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.content[0].title").value(mockPost.getTitle()));

        verify(postRepo).fullTextSearch(ArgumentMatchers.isNull(), ArgumentMatchers.isNull(),
                ArgumentMatchers.any(Pageable.class));
    }

    // --- POST /api/posts (createPost) Tests ---

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER") 
    void testCreatePost_Success() throws Exception {
        CreatePostRequest createRequest = new CreatePostRequest("New Post", "This is the body.",
                new String[] { "java", "spring" });

        when(userRepo.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(tagRepo.findByName("java")).thenReturn(Optional.of(mockTag1));
        when(tagRepo.findByName("spring")).thenReturn(Optional.of(mockTag2));
        when(postRepo.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            savedPost.setId(101L); 
            return savedPost;
        });

        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .with(csrf())) 
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.value.id").value("101"))
                .andExpect(jsonPath("$.value.title").value("New Post"))
                .andExpect(jsonPath("$.value.authorUsername").value("testuser"))
                .andExpect(jsonPath("$.value.tags").isArray())
                .andExpect(jsonPath("$.value.tags").value(hasItems("java", "spring"))) 
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @WithMockUser(username = "nonexistent@example.com", roles = "USER")
    void testCreatePost_Error_UserNotFound() throws Exception {
        CreatePostRequest createRequest = new CreatePostRequest("New Post", "Body", new String[] { "java" });
        when(userRepo.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty()); 

        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized()) 
                .andExpect(jsonPath("$.value").doesNotExist())
                .andExpect(jsonPath("$.error.message").value("User not found"))
                .andExpect(jsonPath("$.error.code").value("401"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void testCreatePost_Error_TagNotFound() throws Exception {
        CreatePostRequest createRequest = new CreatePostRequest("New Post", "Body", new String[] { "java", "unknown" });

        when(userRepo.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(tagRepo.findByName("java")).thenReturn(Optional.of(mockTag1));
        when(tagRepo.findByName("unknown")).thenReturn(Optional.empty()); 

        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest()) 
                .andExpect(jsonPath("$.value").doesNotExist())
                .andExpect(jsonPath("$.error.message").value("Tag not found: unknown"))
                .andExpect(jsonPath("$.error.code").value("400"));
    }

    @Test
    void testCreatePost_Unauthorized_NoAuth() throws Exception {
        CreatePostRequest createRequest = new CreatePostRequest("New Post", "Body", new String[] { "java" });

        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized()); 
    }

    // --- GET /api/posts/{id} (getPostById) Tests ---

    @Test
    void testGetPostById_Success() throws Exception {
        when(postRepo.findById(100L)).thenReturn(Optional.of(mockPost));

        mockMvc.perform(get("/api/posts/{id}", 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.id").value(String.valueOf(mockPost.getId())))
                .andExpect(jsonPath("$.value.title").value(mockPost.getTitle()))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void testGetPostById_Error_NotFound() throws Exception {
        when(postRepo.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/posts/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) 
                .andExpect(jsonPath("$.value").doesNotExist())
                .andExpect(jsonPath("$.error.message").value("Post not found"))
                .andExpect(jsonPath("$.error.code").value("404"));
    }

    // --- DELETE /api/posts/{id} (deletePost) Tests ---

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void testDeletePost_Success() throws Exception {
        when(postRepo.findById(100L)).thenReturn(Optional.of(mockPost));
        when(replyRepo.save(any(Reply.class))).thenReturn(mockReply); 
        doNothing().when(postRepo).delete(any(Post.class)); 

        mockMvc.perform(delete("/api/posts/{id}", 100L)
                .with(csrf())) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").isEmpty()) 
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(postRepo).delete(mockPost); 
        verify(replyRepo).save(mockReply); 
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void testDeletePost_Error_NotFound() throws Exception {
        when(postRepo.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/posts/{id}", 999L)
                .with(csrf()))
                .andExpect(status().isNotFound()) 
                .andExpect(jsonPath("$.value").doesNotExist())
                .andExpect(jsonPath("$.error.message").value("Post not found"))
                .andExpect(jsonPath("$.error.code").value("404"));
    }

    @Test
    @WithMockUser(username = "another@example.com", roles = "USER")
    void testDeletePost_Error_Forbidden() throws Exception {
        User anotherUser = new User(2L, "another@example.com", "pass", "anotheruser");
        Post postByOtherUser = new Post(102L, "Other's Post", "Body", anotherUser, new HashSet<>(), new HashSet<>());

        when(postRepo.findById(102L)).thenReturn(Optional.of(postByOtherUser));

        mockMvc.perform(delete("/api/posts/{id}", 102L)
                .with(csrf()))
                .andExpect(status().isForbidden()) 
                .andExpect(jsonPath("$.value").doesNotExist())
                .andExpect(jsonPath("$.error.message").value("You do not have permission to delete this post"))
                .andExpect(jsonPath("$.error.code").value("403"));
    }

    @Test
    void testDeletePost_Unauthorized_NoAuth() throws Exception {
        mockMvc.perform(delete("/api/posts/{id}", 100L)
                .with(csrf()))
                .andExpect(status().isUnauthorized()); 
    }

    // --- PUT /api/posts/{id} (updatePost) Tests ---

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void testUpdatePost_Success() throws Exception {
        CreatePostRequest updateRequest = new CreatePostRequest("Updated Title", "Updated Body",
                new String[] { "java" });

        when(postRepo.findById(100L)).thenReturn(Optional.of(mockPost)); 
        when(tagRepo.findByName("java")).thenReturn(Optional.of(mockTag1)); 
        when(postRepo.save(any(Post.class))).thenAnswer(invocation -> {
            Post updatedPost = invocation.getArgument(0);
            updatedPost.setTags(new HashSet<>(Collections.singletonList(mockTag1)));
            return updatedPost;
        });

        mockMvc.perform(put("/api/posts/{id}", 100L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.id").value(String.valueOf(mockPost.getId())))
                .andExpect(jsonPath("$.value.title").value("Updated Title"))
                .andExpect(jsonPath("$.value.body").value("Updated Body"))
                .andExpect(jsonPath("$.value.tags").value(hasItem("java"))) 
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void testUpdatePost_Error_PostNotFound() throws Exception {
        CreatePostRequest updateRequest = new CreatePostRequest("Updated Title", "Updated Body",
                new String[] { "java" });
        when(postRepo.findById(anyLong())).thenReturn(Optional.empty()); 

        mockMvc.perform(put("/api/posts/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Post not found"));
    }

    @Test
    @WithMockUser(username = "another@example.com", roles = "USER")
    void testUpdatePost_Error_Forbidden() throws Exception {
        User anotherUser = new User(2L, "another@example.com", "pass", "anotheruser");
        Post postByOtherUser = new Post(102L, "Other's Post", "Body", anotherUser, new HashSet<>(), new HashSet<>());

        when(postRepo.findById(102L)).thenReturn(Optional.of(postByOtherUser)); 

        CreatePostRequest updateRequest = new CreatePostRequest("Updated Title", "Updated Body",
                new String[] { "java" });

        mockMvc.perform(put("/api/posts/{id}", 102L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("You do not have permission to update this post"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void testUpdatePost_Error_TagNotFound() throws Exception {
        CreatePostRequest updateRequest = new CreatePostRequest("Updated Title", "Updated Body",
                new String[] { "java", "unknown" });

        when(postRepo.findById(100L)).thenReturn(Optional.of(mockPost)); 
        when(tagRepo.findByName("java")).thenReturn(Optional.of(mockTag1));
        when(tagRepo.findByName("unknown")).thenReturn(Optional.empty()); 

        mockMvc.perform(put("/api/posts/{id}", 100L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Tag not found: unknown"));
    }

    @Test
    void testUpdatePost_Unauthorized_NoAuth() throws Exception {
        CreatePostRequest updateRequest = new CreatePostRequest("Updated Title", "Updated Body",
                new String[] { "java" });

        mockMvc.perform(put("/api/posts/{id}", 100L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized()); 
    }
}
