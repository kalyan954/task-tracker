package com.nxtwave.tasktracker.refreshToken.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nxtwave.tasktracker.common.exception.UnauthorizedException;
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

    @Override
    public RefreshToken verifyRefreshToken(
            String token
    ) {

        RefreshToken refreshToken = refreshTokenRepository
                        .findByToken(token)
                        .orElseThrow(() ->
                                new UnauthorizedException(
                                        "Invalid refresh token"
                                )
                        );

        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {

            throw new UnauthorizedException(
                    "Refresh token already revoked"
            );
        }

        if (refreshToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            throw new UnauthorizedException(
                    "Refresh token expired"
            );
        }

        return refreshToken;
    }

    @Override
    public void revokeToken(RefreshToken refreshToken) {

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }

}