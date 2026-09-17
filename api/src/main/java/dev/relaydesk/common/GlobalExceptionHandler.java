package dev.relaydesk.common;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return buildResponse("NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND, req, null);
    }

    @ExceptionHandler(IllegalTransitionException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalTransition(IllegalTransitionException ex, HttpServletRequest req) {
        return buildResponse("ILLEGAL_TRANSITION", ex.getMessage(), HttpStatus.CONFLICT, req, null);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLocking(ObjectOptimisticLockingFailureException ex, HttpServletRequest req) {
        // Find current version is hard to extract cleanly from just the exception without querying, 
        // but we return the STALE_VERSION code.
        return buildResponse("STALE_VERSION", "Change request was modified by someone else", HttpStatus.CONFLICT, req, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return buildResponse("VALIDATION_FAILED", ex.getMessage(), HttpStatus.BAD_REQUEST, req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> Map.of("field", e.getField(), "message", e.getDefaultMessage()))
                .collect(Collectors.toList());
        return buildResponse("VALIDATION_FAILED", "Invalid request body", HttpStatus.BAD_REQUEST, req, fieldErrors);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuth(org.springframework.security.core.AuthenticationException ex, HttpServletRequest req) {
        return buildResponse("UNAUTHORIZED", "Invalid email or password", HttpStatus.UNAUTHORIZED, req, null);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex, HttpServletRequest req) {
        return buildResponse("FORBIDDEN", ex.getMessage(), HttpStatus.FORBIDDEN, req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, HttpServletRequest req) {
        ex.printStackTrace();
        return buildResponse("INTERNAL_ERROR", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR, req, null);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(String code, String message, HttpStatus status, HttpServletRequest req, Object fieldErrors) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("message", message);
        body.put("traceId", req.getAttribute("traceId"));
        body.put("fieldErrors", fieldErrors == null ? new Object[0] : fieldErrors);
        return new ResponseEntity<>(body, status);
    }
}
