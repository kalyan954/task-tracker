package com.nxtwave.tasktracker.refreshToken.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nxtwave.tasktracker.refreshToken.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

}