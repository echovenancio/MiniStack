package com.echovenancio.ministack.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.echovenancio.ministack.service.MyUserDetailsService;

import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSecurity
public class SecurityConfig { // No longer extends WebSecurityConfigurerAdapter

    @Autowired
    private JWTFilter filter;
    @Autowired
    private MyUserDetailsService uds; // Still useful for providing UserDetailsService

    // No longer need to autowire UserRepository directly in SecurityConfig
    // if MyUserDetailsService is handling user retrieval

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Replaced .csrf().disable()
                .httpBasic(httpBasic -> httpBasic.disable()) // Replaced .httpBasic().disable()
                .cors(cors -> {
                }) // .cors() without explicit configuration usually means default Spring Boot CORS
                .authorizeHttpRequests(authorize -> authorize // Replaced .authorizeHttpRequests()
                        .requestMatchers("/api/auth/**").permitAll() // Updated to .requestMatchers()
                        .requestMatchers("/api/user/**").hasRole("USER") // Updated to .requestMatchers()
                        .anyRequest().authenticated() // Ensure all other requests are authenticated
                )
                .userDetailsService(uds) // Still works this way
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> response
                                .sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager is now obtained differently
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
