package com.nxtwave.tasktracker.security.config;

import com.nxtwave.tasktracker.security.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth ->
                        auth

                                .requestMatchers(
                                        "/api/v1/auth/**",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/swagger-ui.html"
                                ).permitAll()

                                .anyRequest()
                                .authenticated()
                )

                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint((request, response, authException) ->
                                        writeErrorResponse(
                                                response,
                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                "UNAUTHORIZED",
                                                "Authentication is required"
                                        )
                                )
                                .accessDeniedHandler((request, response, accessDeniedException) ->
                                        writeErrorResponse(
                                                response,
                                                HttpServletResponse.SC_FORBIDDEN,
                                                "FORBIDDEN",
                                                "You do not have permission to access this resource"
                                        )
                                )
                )


                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    private void writeErrorResponse(
            HttpServletResponse response,
            int status,
            String code,
            String message
    ) throws IOException {

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter()
                .write(
                        "{\"status\":"
                                + status
                                + ",\"code\":\""
                                + escapeJson(code)
                                + "\",\"message\":\""
                                + escapeJson(message)
                                + "\"}"
                );
    }

    private String escapeJson(String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
