package com.nxtwave.tasktracker.user.service;

import org.springframework.data.domain.Page;

import com.nxtwave.tasktracker.user.dto.UpdateCurrentUserRequest;
import com.nxtwave.tasktracker.user.dto.UpdateUserRequest;
import com.nxtwave.tasktracker.user.dto.UserResponse;

public interface UserService {

    Page<UserResponse> getUsers(
            int page,
            int limit
    );

    UserResponse getUserById(
            Long userId
    );

    UserResponse updateUser(
            Long userId,
            UpdateUserRequest request
    );

    void deleteUser(
            Long userId
    );

    UserResponse getCurrentUserProfile();

    UserResponse updateCurrentUserProfile(UpdateCurrentUserRequest request);
}
