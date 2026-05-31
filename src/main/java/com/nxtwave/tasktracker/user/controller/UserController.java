package com.nxtwave.tasktracker.user.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nxtwave.tasktracker.task.dto.ApiSuccessResponse;
import com.nxtwave.tasktracker.user.dto.UpdateUserRequest;
import com.nxtwave.tasktracker.user.dto.UserResponse;
import com.nxtwave.tasktracker.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {

        return ResponseEntity.ok(
                userService.getUsers(
                        page,
                        limit
                )
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                userService.getUserById(
                        userId
                )
        );
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @Valid
            @RequestBody
            UpdateUserRequest request
    ) {

        return ResponseEntity.ok(
                userService.updateUser(
                        userId,
                        request
                )
        );
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiSuccessResponse> deleteUser(
            @PathVariable Long userId
    ) {

        userService.deleteUser(
                userId
        );

        return ResponseEntity.ok(
                new ApiSuccessResponse(
                        "User deleted successfully"
                )
        );
    }
}