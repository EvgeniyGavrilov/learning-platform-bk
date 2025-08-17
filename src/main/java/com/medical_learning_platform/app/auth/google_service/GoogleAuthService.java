package com.medical_learning_platform.app.auth.google_service;

import com.medical_learning_platform.app.user.User;
import com.medical_learning_platform.app.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class GoogleAuthService {
    private final WebClient.Builder webClientBuilder;
    private final UserRepository userRepository;
    private final String clientId;
    private final String clientSecret;

    public GoogleAuthService(
            WebClient.Builder webClientBuilder,
            UserRepository userRepository,
            @Value("${google.client-id}") String clientId,
            @Value("${google.client-secret}") String clientSecret
    ) {
        this.webClientBuilder = webClientBuilder;
        this.userRepository = userRepository;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public Mono<GoogleAuthResponse> authenticateWithGoogle(String code) {
        log.info("authenticateWithGoogle code: " + code);
        return exchangeCodeForTokens(code)
            .doOnNext(body -> {
                log.info("authenticateWithGoogle: {}", body);
            })
        .flatMap(tokens -> getUserInfo(tokens.getAccessToken())
            .flatMap(googleUser -> saveOrUpdateUser(googleUser)
//                .map(user -> new GoogleAuthResponse(tokens.getAccessToken(), user.getId())) //  TODO: return custom jwt
                .map(user -> new GoogleAuthResponse("JWT_TOKEN")) //  TODO: return custom jwt
            )
        );
    }



    private Mono<GoogleTokenResponse> exchangeCodeForTokens(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("redirect_uri", "postmessage");
        formData.add("grant_type", "authorization_code");
        log.warn("formData: {}", formData);

        return webClientBuilder.build()
            .post()
            .uri("https://oauth2.googleapis.com/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .bodyToMono(GoogleTokenResponse.class)
            .doOnNext(body -> {
                log.info(body.toString());
            })
            .doOnError(error -> {
                log.error(error.getMessage());
            });
    }

    private Mono<GoogleUserInfo> getUserInfo(String accessToken) {
        log.info("getUserInfo: {}", accessToken);
        return webClientBuilder.build()
            .get()
            .uri("https://www.googleapis.com/oauth2/v2/userinfo")
            .headers(h -> h.setBearerAuth(accessToken))
            .retrieve()
            .bodyToMono(GoogleUserInfo.class)
            .doOnNext(body -> {
                log.info("getUserInfo doOnNext {}", body.toString());
            })
            .doOnError(error -> {
                log.error("getUserInfo doOnError {}", error.getMessage());
            });
    }

    private Mono<User> saveOrUpdateUser(GoogleUserInfo info) {
        log.info("saveOrUpdateUser {}", info);
        return userRepository.findByEmail(info.getEmail())
            .switchIfEmpty(userRepository.save(new User(null, info.getEmail(), info.getName(), info.getPicture())));
    }
}
