package com.nxtwave.tasktracker.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    
    @NotBlank(message = "Refresh token must not be blank")
    private String refreshToken;
}
