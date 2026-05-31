package com.nxtwave.tasktracker.user.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nxtwave.tasktracker.common.exception.ResourceNotFoundException;
import com.nxtwave.tasktracker.common.security.CurrentUserUtil;
import com.nxtwave.tasktracker.user.dto.UpdateUserRequest;
import com.nxtwave.tasktracker.user.dto.UserResponse;
import com.nxtwave.tasktracker.user.entity.User;
import com.nxtwave.tasktracker.user.repository.UserRepository;
import com.nxtwave.tasktracker.user.security.UserAuthorizationService;
import com.nxtwave.tasktracker.user.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserAuthorizationService userAuthorizationService;

    @Override
    public Page<UserResponse> getUsers(
            int page,
            int limit
    ) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        limit
                );

        String email = CurrentUserUtil.getCurrentUserEmail();

        User currentUser = userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        return userRepository.findByOrganizationId(currentUser
                                .getOrganization()
                                .getId(), pageable).map(this::mapToResponse);
    }

    @Override
    public UserResponse getUserById(
            Long userId
    ) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        String email =
        CurrentUserUtil.getCurrentUserEmail();

        User currentUser =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        userAuthorizationService
                .validateOrganizationAccess(
                        user,
                        currentUser
                );

        return mapToResponse(user);
    }

    @Transactional
    @Override
    public UserResponse updateUser(
            Long userId,
            UpdateUserRequest request
    ) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        String email = CurrentUserUtil.getCurrentUserEmail();   
        User currentUser = userRepository.findByEmail(email)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "User not found"
                                    )
                            );

        userAuthorizationService.validateOrganizationAccess(user, currentUser);

        user.setName(
                request.getName()
        );

        user.setRole(
                request.getRole()
        );

        userRepository.save(user);

        return mapToResponse(user);
    }

    @Transactional
    @Override
    public void deleteUser(
            Long userId
    ) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        String email = CurrentUserUtil.getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        userAuthorizationService.validateOrganizationAccess(user,currentUser);

        userRepository.delete(user);
    }

    private UserResponse mapToResponse(
            User user
    ) {

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}