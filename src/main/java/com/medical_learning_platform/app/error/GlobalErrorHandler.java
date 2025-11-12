package com.medical_learning_platform.app.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleResponseStatusException(ResponseStatusException ex) {
//        log.info(ex.getMessage());
        return Mono.just(
            ResponseEntity
                .status(ex.getStatusCode())
                .body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", ex.getStatusCode().value(),
                "error", ex.getReason() != null ? ex.getReason() : "",
                "message", ex.getMessage(),
                "type", "ResponseStatusException",
                "msId", "user-service",
                "source", "GlobalErrorHandler"
                ))
        );
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(Exception ex) {
//        log.info(ex.getMessage());
        return Mono.just(
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 500,
                "error", "Internal Server Error",
                "message", ex.getMessage(),
                "type", "Exception",
                "msId", "user-service",
                "source", "GlobalErrorHandler"
                ))
        );
    }
}
