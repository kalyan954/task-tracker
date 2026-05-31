package com.nxtwave.tasktracker.auth.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class AuthResponse {
    
    String accessToken;
    String refreshToken;
}
