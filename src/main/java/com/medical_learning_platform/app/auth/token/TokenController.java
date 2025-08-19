package com.medical_learning_platform.app.auth.token;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api")
//@CrossOrigin(origins = "http://localhost:4200")
public class TokenController {

    private final TokenService jwtService;

    @PostMapping("/token/refresh")
    public Mono<ResponseEntity<TokenResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {
        log.info("refreshToken");
        return jwtService.validateRefreshToken(request.getRefreshToken())
            .flatMap(user -> {
                String newAccessToken = jwtService.generateAccessToken(user.getUserId(), user.getEmail());
                String newRefreshToken = jwtService.generateRefreshToken(user.getUserId(), user.getEmail()); // опционально
                return Mono.just(ResponseEntity.ok(new TokenResponse(newAccessToken, newRefreshToken)));
            })
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
    }
}
