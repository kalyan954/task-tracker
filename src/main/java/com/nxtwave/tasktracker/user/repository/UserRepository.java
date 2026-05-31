package com.nxtwave.tasktracker.user.repository;

import com.nxtwave.tasktracker.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByOrganizationId(
        Long organizationId,
        Pageable pageable
    );
}