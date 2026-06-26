package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

@Slf4j
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

    private static String requestPath(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        return request.getMethod() + " " + request.getRequestURI();
    }

    private static void logClientError(HttpStatus status, String message, Exception ex, HttpServletRequest request) {
        log.warn("[HTTP_ERROR] status={} path={} exception={} message={}",
                status.value(), requestPath(request), ex.getClass().getSimpleName(), message);
    }

    // --- 400 Bad Request ---

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRequest(InvalidRequestException ex, HttpServletRequest request) {
        String message = resolveMessage(ex, ApiError.INVALID_REQUEST);
        logClientError(HttpStatus.BAD_REQUEST, message, ex, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody(ApiError.INVALID_REQUEST.getErrorLabel(), message, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = "Parâmetro inválido: " + ex.getName();
        logClientError(HttpStatus.BAD_REQUEST, message, ex, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody("Bad Request", message, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + (fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "inválido"))
                .collect(Collectors.joining("; "));
        String message = (details != null && !details.isBlank()) ? details : ApiError.VALIDATION_FAILED.getDefaultMessage();
        logClientError(HttpStatus.BAD_REQUEST, message, ex, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody(ApiError.VALIDATION_FAILED.getErrorLabel(), message, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String message = "Corpo da requisição inválido ou mal formatado.";
        logClientError(HttpStatus.BAD_REQUEST, message, ex, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody(ApiError.VALIDATION_FAILED.getErrorLabel(), message, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = ApiError.VALIDATION_FAILED.getDefaultMessage();
        }
        logClientError(HttpStatus.BAD_REQUEST, message, ex, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody(ApiError.VALIDATION_FAILED.getErrorLabel(), message, HttpStatus.BAD_REQUEST.value()));
    }

    // --- 401 Unauthorized ---

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationFailed(AuthenticationFailedException ex, HttpServletRequest request) {
        String message = resolveMessage(ex, ApiError.AUTHENTICATION_FAILED);
        logClientError(HttpStatus.UNAUTHORIZED, message, ex, request);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorBody(ApiError.AUTHENTICATION_FAILED.getErrorLabel(), message, HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFound(UsernameNotFoundException ex, HttpServletRequest request) {
        String message = ApiError.AUTHENTICATION_FAILED.getDefaultMessage();
        logClientError(HttpStatus.UNAUTHORIZED, message, ex, request);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorBody(ApiError.AUTHENTICATION_FAILED.getErrorLabel(), message, HttpStatus.UNAUTHORIZED.value()));
    }

    // --- 403 Forbidden ---

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
        String message = resolveMessage(ex, ApiError.ACCESS_DENIED);
        logClientError(HttpStatus.FORBIDDEN, message, ex, request);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(errorBody(ApiError.ACCESS_DENIED.getErrorLabel(), message, HttpStatus.FORBIDDEN.value()));
    }

    // --- 404 Not Found ---

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        String message = resolveMessage(ex, ApiError.NOT_FOUND);
        logClientError(HttpStatus.NOT_FOUND, message, ex, request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody(ApiError.NOT_FOUND.getErrorLabel(), message, HttpStatus.NOT_FOUND.value()));
    }

    // --- 409 Conflict ---

    @ExceptionHandler(AppointmentConflictException.class)
    public ResponseEntity<Map<String, Object>> handleAppointmentConflict(AppointmentConflictException ex, HttpServletRequest request) {
        String message = resolveMessage(ex, ApiError.APPOINTMENT_CONFLICT);
        logClientError(HttpStatus.CONFLICT, message, ex, request);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorBody(ApiError.APPOINTMENT_CONFLICT.getErrorLabel(), message, HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEntity(DuplicateEntityException ex, HttpServletRequest request) {
        String message = resolveMessage(ex, ApiError.DUPLICATE_ENTITY);
        logClientError(HttpStatus.CONFLICT, message, ex, request);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorBody(ApiError.DUPLICATE_ENTITY.getErrorLabel(), message, HttpStatus.CONFLICT.value()));
    }

    // --- 422 Unprocessable Entity ---

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidState(InvalidStateException ex, HttpServletRequest request) {
        String message = resolveMessage(ex, ApiError.INVALID_STATE);
        logClientError(HttpStatus.UNPROCESSABLE_ENTITY, message, ex, request);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(errorBody(ApiError.INVALID_STATE.getErrorLabel(), message, HttpStatus.UNPROCESSABLE_ENTITY.value()));
    }

    @ExceptionHandler(ScheduleValidationException.class)
    public ResponseEntity<Map<String, Object>> handleScheduleValidation(ScheduleValidationException ex, HttpServletRequest request) {
        String message = resolveMessage(ex, ApiError.SCHEDULE_VALIDATION);
        logClientError(HttpStatus.UNPROCESSABLE_ENTITY, message, ex, request);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(errorBody(ApiError.SCHEDULE_VALIDATION.getErrorLabel(), message, HttpStatus.UNPROCESSABLE_ENTITY.value()));
    }

    // --- 503 Service Unavailable ---

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleServiceUnavailable(ServiceUnavailableException ex, HttpServletRequest request) {
        ApiError err = ex.getApiError() != null ? ex.getApiError() : ApiError.R2_NOT_CONFIGURED;
        String message = resolveMessage(ex, err);
        HttpStatus status = err.getStatus();
        log.error("[HTTP_ERROR] status={} path={} exception={} message={}",
                status.value(), requestPath(request), ex.getClass().getSimpleName(), message, ex);
        return ResponseEntity.status(status)
                .body(errorBody(status.getReasonPhrase(), message, status.value()));
    }

    // --- 500 Internal Server Error (fallback) ---

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        String message = ApiError.INTERNAL_SERVER_ERROR.getDefaultMessage();
        log.error("[HTTP_ERROR] status={} path={} exception={} message={}",
                HttpStatus.INTERNAL_SERVER_ERROR.value(), requestPath(request), ex.getClass().getSimpleName(), message, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody(ApiError.INTERNAL_SERVER_ERROR.getErrorLabel(), message, HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
