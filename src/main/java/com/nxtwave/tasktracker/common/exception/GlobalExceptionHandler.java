package com.nxtwave.tasktracker.common.exception;

import com.nxtwave.tasktracker.common.response.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "RESOURCE_ALREADY_EXISTS",
                ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {

        FieldError fieldError = ex.getBindingResult().getFieldError();

        String message = fieldError != null
                ? messageOrDefault(fieldError.getDefaultMessage(), "Request validation failed")
                : "Request validation failed";

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                message
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {

        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                ex.getMessage()
        );
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidStatusTransition(InvalidStatusTransitionException ex) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_STATUS_TRANSITION",
                ex.getMessage()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequestParameter(
            MissingServletRequestParameterException ex
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                ex.getParameterName() + " is required"
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {

        String supportedMethods = ex.getSupportedHttpMethods() == null
                ? "none"
                : ex.getSupportedHttpMethods()
                        .stream()
                        .map(HttpMethod::name)
                        .collect(Collectors.joining(", "));

        return buildErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "METHOD_NOT_ALLOWED",
                "HTTP method is not supported. Supported methods: " + supportedMethods
        );
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                getBadRequestMessage(ex)
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException ex) {

        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "Authentication is required"
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {

        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                "You do not have permission to access this resource"
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "DATA_INTEGRITY_VIOLATION",
                "Request violates a database constraint"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception ex) {

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred"
        );
    }

    private String getBadRequestMessage(Exception ex) {

        if (ex instanceof MethodArgumentTypeMismatchException) {
            MethodArgumentTypeMismatchException mismatchException = (MethodArgumentTypeMismatchException) ex;
            return mismatchException.getName() + " has an invalid value";
        }

        if (ex instanceof HttpMessageNotReadableException) {
            return "Request body is missing or contains invalid values";
        }

        if (ex instanceof ConstraintViolationException) {
            ConstraintViolationException constraintViolationException = (ConstraintViolationException) ex;
            String violations = constraintViolationException
                    .getConstraintViolations()
                    .stream()
                    .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                    .collect(Collectors.joining(", "));

            return messageOrDefault(violations, "Request validation failed");
        }

        return messageOrDefault(ex.getMessage(), "Request validation failed");
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(
            HttpStatus status,
            String code,
            String message
    ) {

        return ResponseEntity
                .status(status)
                .body(
                        new ApiErrorResponse(
                                status.value(),
                                code,
                                messageOrDefault(message, "An unexpected error occurred")
                        )
                );
    }

    private String messageOrDefault(String message, String defaultMessage) {

        return message == null || message.isBlank()
                ? defaultMessage
                : message;
    }
}
