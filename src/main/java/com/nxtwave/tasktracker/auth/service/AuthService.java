package com.nxtwave.tasktracker.auth.service;

import com.nxtwave.tasktracker.auth.dto.AuthResponse;
import com.nxtwave.tasktracker.auth.dto.LoginRequest;
import com.nxtwave.tasktracker.auth.dto.RefreshTokenRequest;
import com.nxtwave.tasktracker.auth.dto.RegisterRequest;

public interface AuthService {
    
    void register(RegisterRequest request);
    
    AuthResponse login(LoginRequest request);
    
    AuthResponse refreshToken(RefreshTokenRequest request);
}
