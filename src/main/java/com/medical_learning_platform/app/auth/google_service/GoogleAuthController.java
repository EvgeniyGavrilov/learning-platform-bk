package com.medical_learning_platform.app.auth.google_service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class GoogleAuthController {

    private GoogleAuthService authService;

    @PostMapping("/google")
    public Mono<ResponseEntity<GoogleAuthResponse>> loginWithGoogle(@RequestBody GoogleAuthRequest request) {
        log.info("GoogleAuthRequest request: {}", request.getCode());

        return authService.authenticateWithGoogle(request.getCode())
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
    }

    @PostMapping("/google/callback")
    public Mono<GoogleAuthRequest> callback(@RequestBody GoogleAuthRequest request) {
        log.warn("callback");
        return null;
    }
}
