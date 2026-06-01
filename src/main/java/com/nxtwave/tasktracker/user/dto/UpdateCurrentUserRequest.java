package com.nxtwave.tasktracker.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCurrentUserRequest {

    @NotBlank(message = "Name is required")
    private String name;
}
