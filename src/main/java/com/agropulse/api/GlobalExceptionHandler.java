package com.agropulse.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception e) {
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

        System.err.println("[500] " + chain);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        body.put("detail", chain.toString());
        return ResponseEntity.status(500).body(body);
    }
}
