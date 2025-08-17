package com.medical_learning_platform.app.conf;

import com.medical_learning_platform.app.auth.JwtService;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@AllArgsConstructor
public class SecurityConfig {
    private final JwtService jwtService;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/auth/google").permitAll()
                        .pathMatchers("/api/code").permitAll()
                        .pathMatchers("/api/login-with-code").permitAll()
                        .pathMatchers("/api/register-with-code").permitAll()
                        .pathMatchers("/api/verify-code").permitAll()
                        .pathMatchers("/api/**").permitAll()
                        .pathMatchers("/ipa/**").permitAll()
//                        .anyExchange().authenticated()
                        .anyExchange().permitAll()
                )
//                .httpBasic(Customizer.withDefaults())
                .addFilterAt(jwtAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public AuthenticationWebFilter jwtAuthenticationFilter() {
        ReactiveAuthenticationManager authManager = authentication -> {
            String token = authentication.getCredentials().toString(); // principal = null, credentials = token
            Claims claims = jwtService.validateToken(token);
            String userId = claims.getSubject();
            return Mono.just(new UsernamePasswordAuthenticationToken(userId, token, List.of()));
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



//    @Bean
//    public MapReactiveUserDetailsService userDetailsService() {
//        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
//        UserDetails user = User
//                .withUsername("admin")
//                .password(encoder.encode("admin"))
//                .roles("USER")
//                .build();
//
//        return new MapReactiveUserDetailsService(user);
//    }
}
