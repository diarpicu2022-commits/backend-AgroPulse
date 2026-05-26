package com.agropulse.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Datos de entrada inválidos");
        body.put("fields", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception e) {
        // Build full cause chain for internal logging only — never expose to client
        StringBuilder chain = new StringBuilder();
        Throwable cause = e;
        int depth = 0;
        while (cause != null && depth < 4) {
            if (depth > 0) chain.append(" → ");
            String msg = cause.getMessage();
            chain.append(cause.getClass().getSimpleName())
                 .append(": ")
                 .append(msg != null ? msg : "(null)");
            cause = cause.getCause();
            depth++;
        }
        log.error("[500] {}", chain);

        // Return only a generic message — never expose class names, DB schema, or stack info
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Error interno del servidor. Intenta de nuevo.");
        return ResponseEntity.status(500).body(body);
    }
}
