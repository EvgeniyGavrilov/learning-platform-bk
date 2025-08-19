package com.medical_learning_platform.app.conf;

import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;

@Component
public class JwtConfig {

    @Value("${jwt.access-secret}")
    private String accessSecret;

    @Value("${jwt.refresh-secret}")
    private String refreshSecret;

    @Bean
    public Key accessKey() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(accessSecret));
    }

    @Bean
    public Key refreshKey() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(refreshSecret));
    }
}
