package com.nxtwave.tasktracker.common.exception;

import com.nxtwave.tasktracker.common.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        new ApiErrorResponse(
                                404,
                                "RESOURCE_NOT_FOUND",
                                ex.getMessage()
                        )
                );
        }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        new ApiErrorResponse(
                                409,
                                "RESOURCE_ALREADY_EXISTS",
                                ex.getMessage()
                        )
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {

        String message =ex.getBindingResult()
                        .getFieldError()
                        .getDefaultMessage();

            return ResponseEntity
                    .badRequest()
                    .body(
                            new ApiErrorResponse(
                                    400,
                                    "VALIDATION_ERROR",
                                    message
                            )
                    );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse>handleUnauthorizedException(UnauthorizedException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        new ApiErrorResponse(
                                401,
                                "UNAUTHORIZED",
                                ex.getMessage()
                        )
                );
        }
}