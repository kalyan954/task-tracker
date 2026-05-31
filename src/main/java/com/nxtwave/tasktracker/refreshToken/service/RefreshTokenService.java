package com.nxtwave.tasktracker.refreshToken.service;

import com.nxtwave.tasktracker.refreshToken.entity.RefreshToken;
import com.nxtwave.tasktracker.user.entity.User;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);
}
