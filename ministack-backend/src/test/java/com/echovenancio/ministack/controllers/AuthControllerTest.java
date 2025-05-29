package com.echovenancio.ministack.controllers; 

import com.echovenancio.ministack.config.TestSecurityConfig;
import com.echovenancio.ministack.entity.User;
import com.echovenancio.ministack.models.LoginCredentials;
import com.echovenancio.ministack.models.RegisterRequest;
import com.echovenancio.ministack.repository.UserRepository;
import com.echovenancio.ministack.security.JWTFilter; 
import com.echovenancio.ministack.security.JWTUtil;
import com.echovenancio.ministack.service.MyUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper; 

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException; 
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; 
import org.springframework.security.core.Authentication; 
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; 
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = true)
@ContextConfiguration(classes = TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired 
    private ObjectMapper objectMapper; 

    @MockitoBean 
    private UserRepository userRepo;

    @MockitoBean
    private MyUserDetailsService userDetailsService;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private AuthenticationManager authManager;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void testRegisterSuccess() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "test@example.com", "password123", "password123", "testuser");
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setUsername("testuser");
        savedUser.setPassword("encodedPassword123");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");

        when(userRepo.save(any(User.class))).thenReturn(savedUser);

        when(jwtUtil.generateToken("test@example.com")).thenReturn("mocked-jwt-token-for-test@example.com");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
                .with(csrf())) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt-token").value("mocked-jwt-token-for-test@example.com"));
    }

    @Test
    void testRegisterPasswordsMismatch() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "test@example.com", "password123", "mismatched", "testuser");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Passwords do not match"));
    }

    @Test
    void testRegisterInternalServerError() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "test@example.com", "password123", "password123", "testuser");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");

        when(userRepo.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal server error"));
    }

    @Test
    void testLoginSuccess() throws Exception {
        LoginCredentials loginCredentials = new LoginCredentials(
                "login@example.com", "password123");

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtUtil.generateToken("login@example.com")).thenReturn("mocked-jwt-token-for-login@example.com");

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginCredentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt-token").value("mocked-jwt-token-for-login@example.com"));
    }

    @Test
    void testLoginInvalidCredentials() throws Exception {
        LoginCredentials loginCredentials = new LoginCredentials(
                "wrong@example.com", "wrongpass");

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginCredentials)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void testLoginInternalServerError() throws Exception {
        LoginCredentials loginCredentials = new LoginCredentials(
                "login@example.com", "password123");

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtUtil.generateToken("login@example.com")).thenThrow(new RuntimeException("Token generation error"));

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginCredentials)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal server error"));
    }
}
