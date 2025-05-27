package com.echovenancio.ministack.security;

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
import jakarta.servlet.http.HttpServletResponse;

import com.echovenancio.ministack.service.MyUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JWTFilter jwtFilter;
    private final MyUserDetailsService uds;

    public SecurityConfig(JWTFilter jwtFilter, MyUserDetailsService uds) {
        this.jwtFilter = jwtFilter;
        this.uds = uds;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
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
                        .anyRequest().permitAll())

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
                                (req, res, ex) -> res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden")))

                .userDetailsService(uds);

        // Add your JWT filter before the standard authentication filter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
