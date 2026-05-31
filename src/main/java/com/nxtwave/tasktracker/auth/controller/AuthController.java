package com.nxtwave.tasktracker.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nxtwave.tasktracker.auth.dto.AuthResponse;
import com.nxtwave.tasktracker.auth.dto.LoginRequest;
import com.nxtwave.tasktracker.auth.dto.RegisterRequest;
import com.nxtwave.tasktracker.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }
}
