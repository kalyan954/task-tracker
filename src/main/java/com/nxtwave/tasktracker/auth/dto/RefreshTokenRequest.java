package com.nxtwave.tasktracker.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenRequest {
    
    @NotBlank(message = "Refresh token must not be blank")
    private String refreshToken;
}
