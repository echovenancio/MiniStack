package com.echovenancio.ministack.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;

@TestConfiguration 
@EnableWebSecurity 
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable()) // Explicitly disable form login
                .logout(logout -> logout.disable()) // Also disable logout if you don't use it
                .cors(cors -> {
                }) // use defaults

                // Disable the RequestCache to prevent saving requests and redirecting
                .requestCache(cache -> cache.disable()) // <-- ADD THIS

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Allow auth endpoint
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll() // Allow public access to posts
                        .requestMatchers("/swagger-ui/**").permitAll() // Allow Swagger UI
                        .requestMatchers("/v3/api-docs/**").permitAll() // Allow OpenAPI docs
                        .requestMatchers("/error").permitAll() // Allow error endpoint
                        .requestMatchers("/swagger-ui.html").permitAll() // Allow Swagger UI HTML
                        .anyRequest().authenticated())

                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionFixation().none() // Explicitly disable session fixation for stateless
                )

                .exceptionHandling(eh -> eh
                        // Explicitly set YOUR custom authentication entry point.
                        // This makes sure it doesn't fall back to LoginUrlAuthenticationEntryPoint.
                        .authenticationEntryPoint(
                                (req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                        // You might also want to add an AccessDeniedHandler for 403 Forbidden scenarios
                        .accessDeniedHandler(
                                (req, res, ex) -> res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden")));
        return http.build();
    }
}
