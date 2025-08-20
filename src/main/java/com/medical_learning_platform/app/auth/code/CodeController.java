package com.medical_learning_platform.app.auth.code;

import com.medical_learning_platform.app.auth.token.TokenService;
import com.medical_learning_platform.app.user.User;
import com.medical_learning_platform.app.user.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;


@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class CodeController {

    private CodeService codeService;
    private final UserRepository userRepository;
    private final TokenService jwtService;

    @PostMapping("/login-with-code")
    public Mono<ResponseEntity<Void>> loginWithCode(@RequestBody CodeRequest request) {
        return userRepository.findByEmail(request.getEmail())
            .flatMap(user -> codeService.generateAndSendCode(user.getEmail())
                .thenReturn(ResponseEntity.ok().<Void>build()))
            .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()));
    }

    @PostMapping("/register-with-code")
    public Mono<ResponseEntity<Void>> registerWithCode(@RequestBody CodeRequest request) {
        return userRepository.findByEmail(request.getEmail())
            .flatMap(existingUser -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).<Void>build()))
            .switchIfEmpty(
                userRepository.save(new User(null, request.getEmail(), null, null))
                    .flatMap(user -> codeService.generateAndSendCode(user.getEmail()))
                    .thenReturn(ResponseEntity.ok().<Void>build())
            );
    }

    @PostMapping("/verify-code")
    public Mono<ResponseEntity<VerifyCodeResponse>> verifyCode(@RequestBody VerifyCodeRequest codeRequest) {
        log.info("verifyCode request: {}, {}", codeRequest.getCode(), codeRequest.getEmail());

        return userRepository.findByEmail(codeRequest.getEmail())
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
            .filterWhen(user -> codeService.verifyCode(codeRequest.getEmail(), codeRequest.getCode()))
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid code")))
            .map(user -> ResponseEntity.ok(
                new VerifyCodeResponse(
                    jwtService.generateAccessToken(user.getId(), user.getEmail()),
                    jwtService.generateRefreshToken(user.getId(), user.getEmail())
                )
            ));
    }
}
