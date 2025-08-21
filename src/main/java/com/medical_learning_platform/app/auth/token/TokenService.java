package com.medical_learning_platform.app.auth.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.Date;

@Service
@AllArgsConstructor
public class TokenService {

    private final Key accessKey;
    private final Key refreshKey;

//    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
//    private final Key refreshKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateAccessToken(Long userId, String email) {
        return Jwts.builder()
            .setSubject(String.valueOf(userId))
            .claim("email", email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 3600_000)) // 1 час
//            .setExpiration(new Date(System.currentTimeMillis() + 10000)) // 1 min
            .signWith(accessKey)
            .compact();
    }

    public Claims validateAccessToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(accessKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access token expired", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token", e);
        }
    }

    public String generateRefreshToken(Long userId, String email) {
        return Jwts.builder()
            .setSubject(String.valueOf(userId))
            .claim("email", email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60_000)) // 7 дней
            .signWith(refreshKey)
            .compact();
    }

    public Mono<RefreshTokenData> validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(refreshKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Long userId = Long.parseLong(claims.getSubject());
            String email = claims.get("email", String.class);

            return Mono.just(new RefreshTokenData(userId, email));
        } catch (ExpiredJwtException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired", e));
        } catch (JwtException | IllegalArgumentException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token", e));
        }
    }

}
