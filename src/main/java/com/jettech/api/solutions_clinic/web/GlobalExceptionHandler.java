package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static Map<String, Object> errorBody(String error, String message, int status) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message != null && !message.isBlank() ? message : "Erro não especificado");
        return body;
    }

    /**
     * Resolve mensagem de resposta: se a exceção implementar HasApiError e tiver ApiError definido,
     * usa a mensagem padronizada (formatada com args); caso contrário usa o default do tipo.
     * Nunca usa ex.getMessage() na resposta ao cliente.
     */
    private static String resolveMessage(HasApiError ex, ApiError defaultError) {
        if (ex != null && ex.getApiError() != null) {
            return ex.getApiError().formatMessage(ex.getArgs());
        }
        return defaultError.getDefaultMessage();
    }

    // --- 400 Bad Request ---

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRequest(InvalidRequestException ex) {
        String message = resolveMessage(ex, ApiError.INVALID_REQUEST);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody(ApiError.INVALID_REQUEST.getErrorLabel(), message, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Parâmetro inválido: " + ex.getName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody("Bad Request", message, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + (fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "inválido"))
                .collect(Collectors.joining("; "));
        String message = (details != null && !details.isBlank()) ? details : ApiError.VALIDATION_FAILED.getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody(ApiError.VALIDATION_FAILED.getErrorLabel(), message, HttpStatus.BAD_REQUEST.value()));
    }

    // --- 401 Unauthorized ---

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationFailed(AuthenticationFailedException ex) {
        String message = resolveMessage(ex, ApiError.AUTHENTICATION_FAILED);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorBody(ApiError.AUTHENTICATION_FAILED.getErrorLabel(), message, HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFound(UsernameNotFoundException ex) {
        String message = ApiError.AUTHENTICATION_FAILED.getDefaultMessage();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorBody(ApiError.AUTHENTICATION_FAILED.getErrorLabel(), message, HttpStatus.UNAUTHORIZED.value()));
    }

    // --- 403 Forbidden ---

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex) {
        String message = resolveMessage(ex, ApiError.ACCESS_DENIED);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(errorBody(ApiError.ACCESS_DENIED.getErrorLabel(), message, HttpStatus.FORBIDDEN.value()));
    }

    // --- 404 Not Found ---

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
        String message = resolveMessage(ex, ApiError.NOT_FOUND);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody(ApiError.NOT_FOUND.getErrorLabel(), message, HttpStatus.NOT_FOUND.value()));
    }

    // --- 409 Conflict ---

    @ExceptionHandler(AppointmentConflictException.class)
    public ResponseEntity<Map<String, Object>> handleAppointmentConflict(AppointmentConflictException ex) {
        String message = resolveMessage(ex, ApiError.APPOINTMENT_CONFLICT);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorBody(ApiError.APPOINTMENT_CONFLICT.getErrorLabel(), message, HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEntity(DuplicateEntityException ex) {
        String message = resolveMessage(ex, ApiError.DUPLICATE_ENTITY);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorBody(ApiError.DUPLICATE_ENTITY.getErrorLabel(), message, HttpStatus.CONFLICT.value()));
    }

    // --- 422 Unprocessable Entity ---

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidState(InvalidStateException ex) {
        String message = resolveMessage(ex, ApiError.INVALID_STATE);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(errorBody(ApiError.INVALID_STATE.getErrorLabel(), message, HttpStatus.UNPROCESSABLE_ENTITY.value()));
    }

    @ExceptionHandler(ScheduleValidationException.class)
    public ResponseEntity<Map<String, Object>> handleScheduleValidation(ScheduleValidationException ex) {
        String message = resolveMessage(ex, ApiError.SCHEDULE_VALIDATION);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(errorBody(ApiError.SCHEDULE_VALIDATION.getErrorLabel(), message, HttpStatus.UNPROCESSABLE_ENTITY.value()));
    }

    // --- 503 Service Unavailable ---

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleServiceUnavailable(ServiceUnavailableException ex) {
        ApiError err = ex.getApiError() != null ? ex.getApiError() : ApiError.R2_NOT_CONFIGURED;
        String message = resolveMessage(ex, err);
        HttpStatus status = err.getStatus();
        return ResponseEntity.status(status)
                .body(errorBody(status.getReasonPhrase(), message, status.value()));
    }

    // --- 500 Internal Server Error (fallback) ---

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        ex.printStackTrace();
        String message = ApiError.INTERNAL_SERVER_ERROR.getDefaultMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody(ApiError.INTERNAL_SERVER_ERROR.getErrorLabel(), message, HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
