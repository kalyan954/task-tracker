package com.nxtwave.tasktracker.user.dto;

import com.nxtwave.tasktracker.common.enums.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    @NotBlank(
        message = "Name is required"
    )
    private String name;

    @NotNull(
        message = "Role is required"
    )
    private Role role;
}