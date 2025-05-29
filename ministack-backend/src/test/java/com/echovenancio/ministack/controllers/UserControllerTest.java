package com.echovenancio.ministack.controllers;

import com.echovenancio.ministack.entity.User;
import com.echovenancio.ministack.repository.UserRepository;
import com.echovenancio.ministack.security.JWTFilter; // Adjusted import for JWTFilter
import com.echovenancio.ministack.security.SecurityConfig;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

// IMPORTANT NEW IMPORTS FOR SECURITY TEST UTILITIES
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user; // <--- ADD THIS
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = UserController.class, 
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        JWTFilter.class
}))
@AutoConfigureMockMvc(addFilters = true)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private UserRepository userRepo;

    @Test
    @WithMockUser(username = "email@example.com", roles = { "USER" }) // User "email@example.com"
    void testGetUserDetails() throws Exception {
        User mockUser = new User();
        mockUser.setEmail("email@example.com");

        when(userRepo.findByEmail("email@example.com")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/user/info")
                .with(user("email@example.com"))) // <--- ADD THIS LINE!
                                                  // It ensures the request has the mock user's principal
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("email@example.com"));
    }

}
