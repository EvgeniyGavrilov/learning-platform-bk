package com.medical_learning_platform.app.conf;

import com.medical_learning_platform.app.auth.token.TokenService;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@AllArgsConstructor
public class SecurityConfig {
    private final TokenService jwtService;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(Customizer.withDefaults())
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)  // <- отключаем basic auth
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)  // <- отключаем form login
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers("/api/auth/google").permitAll()
                .pathMatchers("/api/code").permitAll()
                .pathMatchers("/api/login-with-code").permitAll()
                .pathMatchers("/api/register-with-code").permitAll()
                .pathMatchers("/api/verify-code").permitAll()
                .pathMatchers("/api/token/refresh").permitAll()
                .pathMatchers("/api/video/uploaded/{filename:.+}").authenticated()
                .pathMatchers("/api/video/upload").authenticated()
                .pathMatchers("/api/**").permitAll()
                .anyExchange().permitAll()
            )
            .addFilterAt(jwtAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .exceptionHandling(exceptionHandling ->
                exceptionHandling.authenticationEntryPoint((exchange, ex) -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                })
            )
            .build();
    }

    @Bean
    public AuthenticationWebFilter jwtAuthenticationFilter() {
        ReactiveAuthenticationManager authManager = authentication -> {
            try {
                String token = authentication.getCredentials().toString();
                Claims claims = jwtService.validateAccessToken(token);
                String userId = claims.getSubject();

                return Mono.just(new UsernamePasswordAuthenticationToken(userId, token, List.of()));
            } catch (ResponseStatusException e) {
                log.info("jwtAuthenticationFilter error!!!");
                log.info(e.getStatusCode().toString());
                return Mono.error(e);
            }
        };

        ServerAuthenticationConverter converter = exchange ->
            Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(h -> h.startsWith("Bearer "))
                .map(h -> h.substring(7))
                .map(token -> new UsernamePasswordAuthenticationToken(null, token));

        AuthenticationWebFilter filter = new AuthenticationWebFilter(authManager);
        filter.setServerAuthenticationConverter(converter);
        return filter;
    }
}
