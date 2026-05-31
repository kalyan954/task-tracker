package com.nxtwave.tasktracker.refreshToken.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nxtwave.tasktracker.refreshToken.entity.RefreshToken;
import com.nxtwave.tasktracker.refreshToken.repository.RefreshTokenRepository;
import com.nxtwave.tasktracker.refreshToken.service.RefreshTokenService;
import com.nxtwave.tasktracker.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public RefreshToken createRefreshToken(User user) {

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setToken(
                UUID.randomUUID().toString()
        );

        refreshToken.setUser(user);

        refreshToken.setRevoked(false);

        refreshToken.setExpiryDate(LocalDateTime.now()
                        .plusDays(7)
        );

        return refreshTokenRepository.save(refreshToken);
    }
}