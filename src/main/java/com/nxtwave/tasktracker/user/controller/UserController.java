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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
        @Operation(summary = "List Users", description = "Get paginated list of users. Role: ADMIN")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Page of users returned",
                        content = @Content(schema = @Schema(implementation = UserResponse.class))
                ),
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "403", description = "Forbidden")
        })
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
        @Operation(summary = "Get User by ID", description = "Retrieve a user by id. Role: ADMIN")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "User returned",
                        content = @Content(schema = @Schema(implementation = UserResponse.class))
                ),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "403", description = "Forbidden")
        })
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
        @Operation(summary = "Update User", description = "Update user details. Role: ADMIN")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "User updated",
                        content = @Content(schema = @Schema(implementation = UserResponse.class))
                ),
                @ApiResponse(responseCode = "400", description = "Validation error"),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "403", description = "Forbidden")
        })
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
        @Operation(summary = "Delete User", description = "Delete a user by id. Role: ADMIN")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "User deleted",
                        content = @Content(schema = @Schema(implementation = ApiSuccessResponse.class))
                ),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "403", description = "Forbidden")
        })
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