package com.medical_learning_platform.app.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class testController {
    @GetMapping
    public Mono<String> getCurrentUser() {
        log.info("✅ Received request in user-service for /users, user");
        return Mono.just("OK!");
    }
}
