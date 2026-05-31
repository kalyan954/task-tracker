package com.nxtwave.tasktracker.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiErrorResponse {

    private int status;
    private String code;
    private String message;
}
