package com.medical_learning_platform.app.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(String userId, String email) {
        return Jwts.builder()
            .setSubject(userId)
            .claim("email", email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 3600_000)) // 1 час
            .signWith(key)
            .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

}
